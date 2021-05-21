package com.example.unblind.model;

import android.graphics.Bitmap;

import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
/**
 * This class is used as a processing unit for the model,
 * The main functionality is to process the input and output
 * for the model.
 *
 * @author  Team 3
 * @version 1.0
 * @since   05/15/2021
 */
public class BitmapProcessor {
    float[] mean = {0.485f, 0.456f, 0.406f};        // the mean for normalization
    float[] std = {0.229f, 0.224f, 0.225f};         // the std for normalization


    /**
     * This function will preprocess the input bitmap to generate a executable tensor
     *
     * @param bitmap the bitmap contain data about the icon
     * @param size most of the time it will be 224 until further changes
     * @return a tensor for model
     */
    public Tensor preProcess(Bitmap bitmap, int size){

        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false); //      resize the bitmap
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean, this.std);   // generate the tensor

    }

    /**
     * This function will take the tensor as the input, convert it to string based on the the alphabet in Constants
     *
     * @param outputTensor the tensor gathered from model forwarding process
     * @return a string as a label
     */
    public String postProcess(Tensor outputTensor){
        long[] scores = outputTensor.getDataAsLongArray();  // store all scores
        StringBuilder outputLabel = new StringBuilder();    // string for output concatenation
        for (long score : scores) { // mapp through scores
            //outputscore.append(score + " ");
            if(score == 2){     // score 2 means it ended
                break;  // break the loop
            } else if (score != 1){ // score 1 means the start key
                outputLabel.append(Constants.IMAGENET_CLASSES[(int)score] + " ");   // append the word
            }

        }
        return outputLabel.toString();  //return
    }

}
