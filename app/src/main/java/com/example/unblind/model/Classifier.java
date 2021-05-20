package com.example.unblind.model;


import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

/**
 * This class is treated as the model where it has the model as
 * an object, it also uses the processor for processing the input and output
 *
 * @author  Team 3
 * @version 1.0
 * @since   05/15/2021
 */
public class Classifier {

    Module model;
    BitmapProcessor processor;
    /**
     * The constructor for classifier class, it will assign the model and processor
     *
     * @param modelPath this is a string represent the path to the model
     */

    public Classifier(String modelPath){

        model = Module.load(modelPath);     // load the model from path
        processor = new BitmapProcessor();      // initialize a bitmap processor

    }
    /**
     * This function will take in a bitmap and output a string as a prediction accordingly
     *
     * @param bitmap the bitmap store information about the input image
     * @string generated label for particular bitmap
     */
    public String predict(Bitmap bitmap){

        Tensor tensor = processor.preProcess(bitmap,224);       // resize and convert bitmap to tensor.
        IValue inputs = IValue.from(tensor);        // generate values for tensors.
        Tensor outputTensor = model.forward(inputs).toTensor();     // get output from forwarding process using the model.
        String outputLabel = processor.postProcess(outputTensor);   // convert the tensor output to string.

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
