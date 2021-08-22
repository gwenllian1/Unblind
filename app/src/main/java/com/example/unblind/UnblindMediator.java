package com.example.unblind;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

public class UnblindMediator {
    private ArrayList<ColleagueInterface> observers;
    private Pair<Bitmap, String> currentElement = new Pair(null, null);
    public static final String TAG = "UnblindMediator";

    public UnblindMediator() {
        this.observers = new ArrayList<ColleagueInterface>();
    }

    public void addObserver(ColleagueInterface observer){
        Log.e(TAG, "adding observer");
        observers.add(observer);
    }

    public void removeObserver(ColleagueInterface observer){
        Log.e(TAG, "removing observer");
        observers.remove(observer);
    }

    private void notifyObserver(){
        for (ColleagueInterface observer : observers) {
            observer.update();
        }
    }

    public void setElement(Pair<Bitmap, String> element){
        currentElement = element;
        Log.e(TAG, "setting element");
        notifyObserver();
    }

    public Pair<Bitmap, String> getElement(){
        return currentElement;
    }

}