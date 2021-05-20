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

        Tensor tensor = processor.preProcess(bitmap,224);
        IValue inputs = IValue.from(tensor);
        Tensor outputTensor = model.forward(inputs).toTensor();
        String outputLabel = processor.postProcess(outputTensor);

/*      This block of code is used for testing process, remove if not allowed -- Dustin
        float[] inputtensor = tensor.getDataAsFloatArray();
        float total = 0;
        for(float input : inputtensor){
           total += input;
        }
        Log.d("test", total + "");
        Log.d("Test", "predict: " + outputs);
        StringBuilder outputscore=new StringBuilder();
        outputscore.append(outputLabel.toString());*/
        return outputLabel;
    }

}
