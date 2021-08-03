package com.example.unblind.model;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;

public class TfliteClassifier {
    private Context context;

    public TfliteClassifier(Context context){
        this.context = context;
    }

    public String runObjectDetection(Bitmap bitmap) throws IOException {
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
        TensorImage image = TensorImage.fromBitmap(bitmap);
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(1)
                .setScoreThreshold(0.5f)
                .build();
        // the application context
        // must be same as the filename in assets folder
        ObjectDetector detector = ObjectDetector.createFromFileAndOptions(
                context, // the application context
                "icons50model.tflite", // must be same as the filename in assets folder
                options
        );
        List<Detection> results = detector.detect(image);
        for (Detection detection : results){
            List<Category> categories = detection.getCategories();
            for (Category category : categories){
                return category.getLabel();
            }
        }
        return "fail";
    }
}
