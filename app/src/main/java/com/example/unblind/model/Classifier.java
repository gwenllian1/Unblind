package com.example.unblind.model;


import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;


public class Classifier {

    Module model;
    BitmapProcessor processor;

    public Classifier(String modelPath){

        model = Module.load(modelPath);
        processor = new BitmapProcessor();

    }

    public String predict(Bitmap bitmap){

        Tensor tensor = processor.preprocess(bitmap,224);

        float[] inputtensor = tensor.getDataAsFloatArray();
        float total = 0;
        for(float input : inputtensor){
           total += input;
        }
        Log.d("test", total + "");
        StringBuilder strbul=new StringBuilder();
        IValue inputs = IValue.from(tensor);

        Tensor outputs = model.forward(inputs).toTensor();
        //Log.d("Test", "predict: " + outputs);
        long[] scores = outputs.getDataAsLongArray();

        StringBuilder outputscore=new StringBuilder();
        for (long score : scores) {
            outputscore.append(score + " ");
            if(score == 2){
                break;
            } else if (score != 1){
                strbul.append(Constants.IMAGENET_CLASSES[(int)score] + " ");
            }

        }
        outputscore.append(strbul.toString());
        return outputscore.toString() ;
    }

}
