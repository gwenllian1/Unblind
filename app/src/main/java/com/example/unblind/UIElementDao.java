package com.example.unblind;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UIElementDao {
    @Query("SELECT * FROM uiElement")
    List<UIElement> getAll();

    @Query("SELECT * FROM uiElement WHERE iconHash IN (:iconHashes)")
    List<UIElement>  loadAllByIds(String[] iconHashes);

    @Insert
    void insertAll(UIElement ... uiElements);

}
