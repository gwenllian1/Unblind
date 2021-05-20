package com.example.unblind;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class UIElement {
    public UIElement(String iconHash, String altText) {
        this.iconHash = iconHash;
        this.altText = altText;
    }
    @PrimaryKey
    @NonNull
    public String iconHash;

    @ColumnInfo(name = "alt_text")
    public String altText;
}
