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
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TfliteClassifier {
    private final ImageProcessor imageProcessor;
    private final List<String> labels;
    private final TensorProcessor probabilityPostProcessor;
    private final Model model;

    public TfliteClassifier(Context context, Model.Options options) throws
            IOException {
        model = Model.createModel(context, "icons50model.tflite", options);
        MetadataExtractor extractor = new MetadataExtractor(model.getData());
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
    }

    public static TfliteClassifier newInstance(Context context) throws IOException {
        return new TfliteClassifier(context, (new Model.Options.Builder()).build());
    }

    public static TfliteClassifier newInstance(Context context, Model.Options options)
            throws IOException {
        return new TfliteClassifier(context, options);
    }

    public Outputs process(TensorImage image) {
        TensorImage processedimage = imageProcessor.process(image);
        Outputs outputs = new Outputs(model);
        model.run(new Object[] {processedimage.getBuffer()}, outputs.getBuffer());
        return outputs;
    }

    public void close() {
        model.close();
    }

    public Outputs process(TensorBuffer image) {
        TensorBuffer processedimage = image;
        Outputs outputs = new Outputs(model);
        model.run(new Object[] {processedimage.getBuffer()}, outputs.getBuffer());
        return outputs;
    }

    public class Outputs {
        private TensorBuffer probability;

        private Outputs(Model model) {
            this.probability = TensorBuffer.createFixedSize(model.getOutputTensorShape(0), DataType.UINT8);
        }

        public List<Category> getProbabilityAsCategoryList() {
            return new TensorLabel(labels, probabilityPostProcessor.process(probability)).getCategoryList();
        }

        public TensorBuffer getProbabilityAsTensorBuffer() {
            return probabilityPostProcessor.process(probability);
        }

        private Map<Integer, Object> getBuffer() {
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, probability.getBuffer());
            return outputs;
        }
    }
}
