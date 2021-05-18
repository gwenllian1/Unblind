package com.example.unblind;

import android.util.Pair;

import java.util.List;

public class UnblindMediator {
    private List<ColleagueInterface> observers;
    private Pair<String, String> currentElement;

    public void addObserver(ColleagueInterface observer){
        observers.add(observer);
    }

    private void notifyObserver(){
        for (ColleagueInterface observer : observers) {
            observer.update();
        }
    }

    public void setElement(Pair<String, String> element){
        currentElement = element;
        notifyObserver();
    }

    public Pair<String, String> getElement(){
        return currentElement;
    }

}
