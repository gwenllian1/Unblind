package com.example.unblind;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;

public class UnblindMediator {
    private ArrayList<ColleagueInterface> observers;
    private Queue<Pair<Bitmap, String>> IncomingQueue = new ArrayDeque<>();
    private Queue<Pair<Bitmap, String>> OutgoingQueue = new ArrayDeque<>();
//    private Pair<Bitmap, String> currentElement = new Pair(null, null);
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

    public void notifyObservers(){
        for (ColleagueInterface observer : observers) {
            observer.update();
        }
    }

    public boolean checkIncomingEmpty(){
        return IncomingQueue.isEmpty();
    }

    public boolean checkOutgoingEmpty() { return OutgoingQueue.isEmpty(); }

    public boolean checkIncomingSizeMoreThanOne(){
        return (IncomingQueue.size() > 1);
    }

    public void pushElementToIncoming(Pair<Bitmap, String> element){
        IncomingQueue.add(element);
        Log.e(TAG, "setting element");
    }

    public Pair<Bitmap, String> getElementFromIncoming(){
        return IncomingQueue.peek();
    }

    public Pair<Bitmap, String> serveElementFromIncoming(){
        return IncomingQueue.remove();
    }

    public void pushElementToOutgoing(Pair<Bitmap, String> element){
        OutgoingQueue.add(element);
        Log.e(TAG, "setting element");
    }

    public Pair<Bitmap, String> getElementFromOutgoing(){
        return OutgoingQueue.peek();
    }

    public Pair<Bitmap, String> serveElementFromOutgoing(){
        return OutgoingQueue.remove();
    }



}
