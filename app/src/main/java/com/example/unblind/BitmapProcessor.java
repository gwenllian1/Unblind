package com.example.unblind;

import android.graphics.Bitmap;

import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

public class BitmapProcessor {
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};

    public Tensor preprocess(Bitmap bitmap, int size){

        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean,this.std);

    }
}
