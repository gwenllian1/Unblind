package com.example.unblind.model;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.InputStream;

public class ModelBlackBoxTesting {
    public Classifier classifier;
    public InputStream inputStream;
    public Bitmap bitmap;
    public String[] imageFileNames = { "3.png","56.png","64.png","86.png","2002.png"};
    private Context context;

    public ModelBlackBoxTesting(Context context){
        this.context=context;
    }

    public static void main(String[] args){

    }

    public void loadClassifier(){
        classifier = new Classifier(Utils.assetFilePath(context, "labeldroid.pt"));
    }
}
