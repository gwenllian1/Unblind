package com.example.unblind;

import android.graphics.Bitmap;

public class UnblindDataObject {
    public Bitmap iconImage;
    public String iconLabel;
    public boolean batchStatus;
    public boolean isClickable;

    public UnblindDataObject(Bitmap initIconImage, String initIconLabel, boolean initBatchStatus) {
        this.batchStatus = initBatchStatus;
        this.iconImage = initIconImage;
        this.iconLabel = initIconLabel;
        this.isClickable = false;
    }

    public boolean getIsClickable(){
        return this.isClickable;
    }
}
