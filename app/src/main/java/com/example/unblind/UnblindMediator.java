package com.example.unblind;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class UnblindMediator {
    private ArrayList<ColleagueInterface> observers;
    private Pair<String, String> currentElement;
    public static final String TAG = "UnblindMediator";

    public UnblindMediator() {
        this.observers = new ArrayList<ColleagueInterface>();
    }

    public void addObserver(ColleagueInterface observer){
        Log.e(TAG, "adding observer");
        observers.add(observer);
    }

    private void notifyObserver(){
        for (ColleagueInterface observer : observers) {
            observer.update();
        }
    }

    public void setElement(Pair<String, String> element){
        currentElement = element;
        Log.e(TAG, "setting element");
        notifyObserver();
    }

    public Pair<String, String> getElement(){
        return currentElement;
    }

}
