package com.example.unblind;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;

public class UnblindMediator {
    private ArrayList<ColleagueInterface> observers;
    private Queue<UnblindDataObject> incomingImmediateQueue = new ArrayDeque<>();
    private Queue<UnblindDataObject> incomingBatchQueue = new ArrayDeque<>();
    private Queue<UnblindDataObject> outgoingQueue = new ArrayDeque<>();
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

    public boolean checkModelServiceObserver() {
        for (ColleagueInterface observer : observers) {
            if (observer.getClass() == ModelService.class) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIncomingImmediateQueueEmpty(){
        return incomingImmediateQueue.isEmpty();
    }

    public boolean checkIncomingImmediateQueueSizeMoreThanOne(){
        return (incomingImmediateQueue.size() > 1);
    }

    public void pushElementToIncomingImmediateQueue(UnblindDataObject element){
        incomingImmediateQueue.add(element);
        Log.e(TAG, "adding incoming element");
    }

    public UnblindDataObject getElementFromIncomingImmediateQueue(){
        return incomingImmediateQueue.peek();
    }

    public UnblindDataObject serveElementFromIncomingImmediateQueue(){
        return incomingImmediateQueue.remove();
    }

    public boolean checkIncomingBatchQueueEmpty(){
        return incomingBatchQueue.isEmpty();
    }

    public boolean checkIncomingBatchQueueSizeMoreThanOne(){
        return (incomingBatchQueue.size() > 1);
    }

    public void pushElementToIncomingBatchQueue(UnblindDataObject element){
        incomingBatchQueue.add(element);
        Log.e(TAG, "adding incoming element");
    }

    public UnblindDataObject getElementFromIncomingBatchQueue(){
        return incomingBatchQueue.peek();
    }

    public UnblindDataObject serveElementFromIncomingBatchQueue(){
        return incomingBatchQueue.remove();
    }

    public void pushElementToOutgoingImmediateQueue(UnblindDataObject element){
        outgoingQueue.add(element);
        Log.e(TAG, "adding outgoing element");
    }

    public UnblindDataObject getElementFromOutgoingImmediateQueue(){
        return outgoingQueue.peek();
    }

    public UnblindDataObject serveElementFromOutgoingImmediateQueue(){
        return outgoingQueue.remove();
    }

    public boolean checkOutgoingImmediateQueueEmpty() { return outgoingQueue.isEmpty(); }

}
