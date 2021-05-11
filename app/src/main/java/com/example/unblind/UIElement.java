package com.example.unblind;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UIElement {
    @PrimaryKey
    public String iconHash;

    @ColumnInfo(name = "alt_text")
    public String altText;
}
