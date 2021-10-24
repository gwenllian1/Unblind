//        Copyright 2021 Project 3

//        This file is part of UnBlind.
//
//        UnBlind is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        UnBlind is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with UnBlind.  If not, see <https://www.gnu.org/licenses/>.

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
 * @version 1.1
 * @since   12/10/2021
 */
public class TfliteTesting {
    private TfliteClassifier tfliteClassifier;       // classifier object
    private InputStream inputStream;     // the input to load bitmap
    private ArrayList<Bitmap> testImages = new ArrayList<>();        // store all the testing image as bitmaps
    private String[] imageFileNames = {"3.png", "56.png", "64.png", "86.png", "2002.png", "11978.png"};  //  <-- add image to test here ////////
    private ArrayList<String> availableNames = new ArrayList<>();   // mapping name for available bitmaps
    private Context context;        // context for operating function that needs
    private ArrayList<String> predictions = new ArrayList<>(); // store all outputs from model


    /**
     * This is a constructor for testing classes, will be called to initialize the blackbox test
     *
     * @param context Current context of the activity which is using this
     * @return no return (void)
     */
    public TfliteTesting(Context context) throws IOException {
        this.context = context;   // store context
        main(new String[]{"test", "test"}); // call main function ???
    }


    /**
     * This is the main function which gets executed once the test is created
     *
     * @param args an array of strings for any additional variables needed
     * @return no return (void)
     */
    private void main(String[] args) throws IOException {
        loadClassifier();
        loadImages();
        runPrediction();
        displayResult();
    }

    /**
     * This function will load the image classifier, store it in classifier attributes
     */
    private void loadClassifier() throws IOException {
        // use the function provided by Utils class
        tfliteClassifier = new TfliteClassifier(context);
    }

    /**
     * This function will load all images specified in line 26, store them as separate bitmap
     * and file name for whichever available image name.
     */
    private void loadImages() {
        for (String imageFileName : imageFileNames) {         // loop through filenames
            Bitmap tempBitmap = null;   // initialize a bitmap for temporary storing loaded result
            try {   // try if file is available
                inputStream = context.getAssets().open(imageFileName);  // get the file
                tempBitmap = BitmapFactory.decodeStream(inputStream);   // store in the temporary bitmap
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
    private void runPrediction() throws IOException {
        int index = 0;
        for (Bitmap bitmap : testImages) {     // loop through all bitmap
            String result = tfliteClassifier.predict(bitmap); // predict the bitmap

            String output = "Image = " + availableNames.get(index) + ", Label = " + result; // append the result with the filename
            predictions.add(output); // store output

            index += 1;     // increment the index for mapping the label
        }
        // Releases model resources if no longer used.
        tfliteClassifier.close();
    }

    /**
     * This function will log all the result into the terminal
     */
    private void displayResult() {
        for (String prediction : predictions) {
            Log.d("Team 3 Model Result", prediction);
        }
    }
}
