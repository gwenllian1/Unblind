package com.example.unblind.model;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * TfliteClassifier uses a pre-trained TensorFlow Lite model in order to perform image classification.
 * It is used to predict Android mobile app icons and provide them with an appropriate label.
 * It's 'predict' method is to be used by calling code, which will be required to pass a Bitmap
 * representation of the icon image to be labelled, and a string with a description of the label
 * will be returned by the method.
 *
 * Authors: Josie Leye & Llio Vernon
 * Last Edit: 23/08/2021
 *
 * */
public class TfliteClassifier {
    private ImageProcessor imageProcessor;
    private List<String> labels;
    private TensorProcessor probabilityPostProcessor;
    private Model model;

    public TfliteClassifier(Context context) {
        setup(context);
    }

    private void setup(Context context){
        try {
            model = modelBuild(context, new Model.Options.Builder().build());
            MetadataExtractor extractor = null;
            extractor = new MetadataExtractor(model.getData());
            ImageProcessor.Builder imageProcessorBuilder = new ImageProcessor.Builder()
                    .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                    .add(new NormalizeOp(new float[] {0.0f}, new float[] {255.0f}))
                    .add(new QuantizeOp(0f, 0.003921569f))
                    .add(new CastOp(DataType.UINT8));
            imageProcessor = imageProcessorBuilder.build();
            TensorProcessor.Builder probabilityPostProcessorBuilder = new TensorProcessor.Builder()
                    .add(new DequantizeOp((float)0, (float)0.00390625))
                    .add(new NormalizeOp(new float[] {0.0f}, new float[] {1.0f}));
            probabilityPostProcessor = probabilityPostProcessorBuilder.build();
            labels = FileUtil.loadLabels(extractor.getAssociatedFile("labels.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Model modelBuild(Context context, Model.Options options) throws IOException {
        return Model.createModel(context, "icons-classifier_model_final.tflite", options);
    }

    public TflitePrediction predict(Bitmap bitmap){
        TensorImage image = TensorImage.fromBitmap(bitmap);
        TensorImage processedImage = imageProcessor.process(image);
        Outputs outputs = new Outputs(model);
        model.run(new Object[] {processedImage.getBuffer()}, outputs.getBuffer());
        List<Category> probabilityList = outputs.getProbabilityAsCategoryList();
        float maxScore = 0;
        int bestPredictedCategoryIndex = 0;
        for (Category category : probabilityList){
            if (category.getScore() > maxScore){
                maxScore = category.getScore();
                bestPredictedCategoryIndex = probabilityList.indexOf(category);
            }
        }
        String label = probabilityList.get(bestPredictedCategoryIndex).getLabel();
        TflitePrediction prediction = new TflitePrediction(label, maxScore);
        return prediction;
    }

    public void close() {
        model.close();
    }

    private class Outputs {
        private TensorBuffer probability;

        private Outputs(Model model) {
            this.probability = TensorBuffer.createFixedSize(model.getOutputTensorShape(0), DataType.UINT8);
        }

        private List<Category> getProbabilityAsCategoryList() {
            return new TensorLabel(labels, probabilityPostProcessor.process(probability)).getCategoryList();
        }

        private Map<Integer, Object> getBuffer() {
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, probability.getBuffer());
            return outputs;
        }
    }
}
