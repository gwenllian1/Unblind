package com.example.unblind;


import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;


public class Classifier {

    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};

    public Classifier(String modelPath){

        model = Module.load(modelPath);

    }

//    public void setMeanAndStd(float[] mean, float[] std){
//
//        this.mean = mean;
//        this.std = std;
//    }

    public Tensor preprocess(Bitmap bitmap, int size){

        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean,this.std);

    }

    public String predict(Bitmap bitmap){

        Tensor tensor = preprocess(bitmap,224);

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
