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
