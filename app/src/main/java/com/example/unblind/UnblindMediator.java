package com.example.unblind;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class UnblindMediator {
    public static final String TAG = "UnblindMediator";
    private final ArrayList<ColleagueInterface> observers;
    private final Queue<UnblindDataObject> IncomingQueue = new ArrayDeque<>();
    private final Queue<UnblindDataObject> OutgoingQueue = new ArrayDeque<>();

    public UnblindMediator() {
        this.observers = new ArrayList<ColleagueInterface>();
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        // Get a Base64 encoded PNG of the button bitmap
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void addObserver(ColleagueInterface observer) {
        Log.d(TAG, "adding observer");
        observers.add(observer);
    }

    public void removeObserver(ColleagueInterface observer) {
        Log.d(TAG, "removing observer");
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (ColleagueInterface observer : observers) {
            observer.update();
        }
    }

    public boolean checkIncomingEmpty() {
        return IncomingQueue.isEmpty();
    }

    public boolean checkOutgoingEmpty() {
        return OutgoingQueue.isEmpty();
    }

    public boolean checkIncomingSizeMoreThanOne() {
        return (IncomingQueue.size() > 1);
    }

    public void pushElementToIncoming(UnblindDataObject element) {
        IncomingQueue.add(element);
        Log.v(TAG, "adding incoming element");
    }

    public UnblindDataObject getElementFromIncoming() {
        return IncomingQueue.peek();
    }

    public UnblindDataObject serveElementFromIncoming() {
        return IncomingQueue.remove();
    }

    public void pushElementToOutgoing(UnblindDataObject element) {
        OutgoingQueue.add(element);
        Log.v(TAG, "adding outgoing element");
    }

    public UnblindDataObject getElementFromOutgoing() {
        return OutgoingQueue.peek();
    }

    public UnblindDataObject serveElementFromOutgoing() {
        return OutgoingQueue.remove();
    }


}
