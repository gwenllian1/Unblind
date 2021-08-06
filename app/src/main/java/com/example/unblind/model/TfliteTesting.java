package com.example.unblind.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides blackbox testing for the model itself
 * Instruction:
 * 1/ Add images to assets folder
 * 2/ Add the file name into imageFileNames variable below
 * 3/ Run the test by clicking the button in the app
 *
 * @author Team 3
 * @version 1.0
 * @since   05/15/2021
 */
public class TfliteTesting {
    public TfliteClassifier tfliteClassifier;       // classifier object
    public InputStream inputStream;     // the input to load bitmap
    public ArrayList<Bitmap> testImages = new ArrayList<>();        // store all the testing image as bitmaps
    public String[] imageFileNames = {"3.png", "56.png", "64.png", "86.png", "2002.png"};  //  <-- add image to test here ////////
    private ArrayList<String> availableNames = new ArrayList<>();   // mapping name for available bitmaps
    private Context context;        // context for operating function that needs
    public ArrayList<String> predictions = new ArrayList<>(); // store all outputs from model


    /**
     * This is a constructor for testing classes, will be called to initialize the blackbox test
     *
     * @param context Current context of the activity which is using this
     * @return no return (void)
     */
    public TfliteTesting(Context context) throws IOException {
        this.context = context;   // store context
        System.out.println("Start testing");
        main(new String[]{"test", "test"}); // call main function ???
        System.out.println("Testing complete");
    }


    /**
     * This is the main function which gets executed once the test is created
     *
     * @param args an array of strings for any additional variables needed
     * @return no return (void)
     */
    public void main(String[] args) throws IOException {
        loadClassifier();
        System.out.println("Classifier loaded succesfully");
        loadImages();
        System.out.println("Images loaded succesfully");
        runPrediction();
        System.out.println("Prediction ran succesfully");
        displayResult();
    }

    /**
     * This function will load the image classifier, store it in classifier attributes
     */
    public void loadClassifier() throws IOException {
        // use the function provided by Utils class
        System.out.println("Start load classifier");
        tfliteClassifier = TfliteClassifier.newInstance(context);
    }

    /**
     * This function will load all images specified in line 26, store them as separate bitmap
     * and file name for whichever available image name.
     */
    public void loadImages() {
        System.out.println("Start load images");
        for (String imageFileName : imageFileNames) {         // loop through filenames
            Bitmap tempBitmap = null;   // initialize a bitmap for temporary storing loaded result
            try {   // try if file is available
                inputStream = context.getAssets().open(imageFileName);  // get the file
                System.out.println("Opened image");
                tempBitmap = BitmapFactory.decodeStream(inputStream);   // store in the temporary bitmap
                System.out.println("Decoded image");
            } catch (IOException e) {   // catch error
                e.printStackTrace();
            }
            if (tempBitmap != null) {      // check for null means no bitmap loaded
                testImages.add(tempBitmap);         // add the image
                availableNames.add(imageFileName);  // add the names accordingly for logging

            }

        }
    }

    /**
     * This function will loop through all available images, pass them into the model
     * and store the output in the prediction array
     */
    public void runPrediction() throws IOException {
        System.out.println("Start run prediction");
        int index = 0;
        for (Bitmap bitmap : testImages) {     // loop through all bitmap
            TensorImage image = TensorImage.fromBitmap(bitmap);
            TfliteClassifier.Outputs outputs = tfliteClassifier.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            float s = 0;
            Category obj = null;
            for (Category c : probability){
                if (c.getScore() > s){
                    s = c.getScore();
                    obj = c;
                }
            }
            String result = probability.get(probability.indexOf(obj)).getLabel();
            String output = "Image = " + availableNames.get(index) + ", Label = " + result + ", Probability = " + s;
            predictions.add(output);
            System.out.println(output);

//            String result = tfliteClassifier.runObjectDetection(bitmap);     // predict the bitmap
//            String output = availableNames.get(index) + ": " + result;  // append the result with the filename
//            predictions.add(output);    // store output
            index += 1;     // increment the index for mapping the label
        }
        // Releases model resources if no longer used.
        tfliteClassifier.close();
    }

    /**
     * This function will log all the result into the terminal
     */
    public void displayResult() {
        System.out.println("Start displaying results");
        for (String prediction : predictions) {
            Log.d("Team 3 Model Result", prediction);
        }
    }
}
