package com.example.unblind.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ModelBlackBoxTesting {
    public Classifier classifier;
    public InputStream inputStream;
    public ArrayList<Bitmap> testImages = new ArrayList<>();
    public String[] imageFileNames = { "3.png","56.png","64.png","86.png","2002.png"};
    private ArrayList<String> availableNames = new ArrayList<>();
    private Context context;
    public ArrayList<String> predictions = new ArrayList<>();

    public ModelBlackBoxTesting(Context context){
        this.context=context;
        main(new String[]{"test", "test"});
    }

    public void main(String[] args){
        loadClassifier();
        loadImages();
        runPrediction();
        displayResult();
    }

    public void loadClassifier(){
        classifier = new Classifier(Utils.assetFilePath(context, "labeldroid.pt"));
    }

    public void loadImages(){
        for(String imageFileName : imageFileNames){
            Bitmap tempBitmap = null;
            try {
                inputStream = context.getAssets().open(imageFileName);
                tempBitmap = BitmapFactory.decodeStream(inputStream);
                Log.d("nani",imageFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(tempBitmap!= null){
                testImages.add(tempBitmap);
                availableNames.add(imageFileName);

            }

        }
    }

    public void runPrediction(){
        int index = 0;
        for(Bitmap bitmap: testImages){
            String result = classifier.predict(bitmap);
            String output = availableNames.get(index) + ": " + result;
            predictions.add(output);
            index += 1;
        }
    }
    public void displayResult(){
        for(String prediction: predictions){
            Log.d("Team 3 Model Result", prediction);
        }
    }



}
