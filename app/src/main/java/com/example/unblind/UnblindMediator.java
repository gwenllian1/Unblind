package com.example.unblind;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;

public class UnblindMediator {
    private ArrayList<ColleagueInterface> observers;
    private Queue<UnblindDataObject> IncomingQueue = new ArrayDeque<>();
    private Queue<UnblindDataObject> OutgoingQueue = new ArrayDeque<>();
    public static final String TAG = "UnblindMediator";

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        // Get a Base64 encoded PNG of the button bitmap
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

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

    public void pushElementToIncoming(UnblindDataObject element){
        IncomingQueue.add(element);
        Log.e(TAG, "adding incoming element");
    }

    public UnblindDataObject getElementFromIncoming(){
        return IncomingQueue.peek();
    }

    public UnblindDataObject serveElementFromIncoming(){
        return IncomingQueue.remove();
    }

    public void pushElementToOutgoing(UnblindDataObject element){
        OutgoingQueue.add(element);
        Log.e(TAG, "adding outgoing element");
    }

    public UnblindDataObject getElementFromOutgoing(){
        return OutgoingQueue.peek();
    }

    public UnblindDataObject serveElementFromOutgoing(){
        return OutgoingQueue.remove();
    }



}
