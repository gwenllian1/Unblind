//        Copyright 2021 Project 3

//        This file is part of UnBlind.
//
//        UnBlind is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        UnBlind is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with UnBlind.  If not, see <https://www.gnu.org/licenses/>.

package com.example.unblind;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * UnblindMediator which dictates the communication between the UnblindAccessibilityService and the
 * ModelService, passing icon image bitmaps and generated descriptive labels between the two.
 */
public class UnblindMediator {
    private ArrayList<ColleagueInterface> observers = new ArrayList<>();
    private Queue<UnblindDataObject> incomingImmediateQueue = new ArrayDeque<>();
    private Queue<UnblindDataObject> incomingBatchQueue = new ArrayDeque<>();
    private Queue<UnblindDataObject> outgoingQueue = new ArrayDeque<>();
    public static final String TAG = "UnblindMediator";

    /**
     * Converts a bitmap into a byte array.
     *
     * @param bitmap Bitmap to be converted into a byte array.
     * @return Byte array conversion of bitmap input.
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        // Get a Base64 encoded PNG of the button bitmap
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Adds an observer to the UnblindMediator.
     * @param observer Observer object to the UnblindMediator implementing the ColleageInterface.
     *                 interface
     */
    public void addObserver(ColleagueInterface observer) {
        Log.d(TAG, "adding observer");
        observers.add(observer);
    }

    /**
     * Removes an observer from the UnblindMediator.
     * @param observer Existing observer object to be removed from the UnblindMediator.
     */
    public void removeObserver(ColleagueInterface observer) {
        Log.d(TAG, "removing observer");
        observers.remove(observer);
    }

    /**
     * Notify all observers of the UnblindMediator to update themselves based on the state of the
     * mediator.
     */
    public void notifyObservers() {
        for (ColleagueInterface observer : observers) {
            observer.update();
        }
    }

    /**
     * Checks whether or not there is a ModelService currently observing the UnblindMediator.
     * @return true if a ModelService is observing the mediator, and false otherwise.
     */
    public boolean checkModelServiceObserver() {
        Log.v(TAG, "checking if any observers are ModelService");
        for (ColleagueInterface observer : observers) {
            Log.v(TAG, String.valueOf(observer.getClass()));
            if (observer.getClass() == ModelService.class) {
                Log.v(TAG, "observers are ModelService");
                return true;
            }
        }
        Log.v(TAG, "observers are not ModelService");
        return false;
    }

    /**
     * Checks if the incoming queue for immediate icon processing is empty.
     * @return true if the incoming immediate queue is empty, false otherwise.
     */
    public boolean checkIncomingImmediateQueueEmpty() {
        return incomingImmediateQueue.isEmpty();
    }

    /**
     * Checks if the incoming queue for immediate icon processing contains more than one element.
     * @return true if there is more than one element in the incoming immediate queue, false
     * otherwise.
     */
    public boolean checkIncomingImmediateQueueSizeMoreThanOne() {
        return (incomingImmediateQueue.size() > 1);
    }

    /**
     * Pushes an element into the back of the incoming queue for immediate icon processing.
     * @param element Element to be pushed into the queue.
     */
    public void pushElementToIncomingImmediateQueue(UnblindDataObject element) {
        incomingImmediateQueue.add(element);
        Log.d(TAG, "adding incoming element to immediate queue");
    }

    /**
     * Peeks at the element at the front of the incoming queue for immediate icon processing.
     * @return The element at the front of the queue.
     */
    public UnblindDataObject getElementFromIncomingImmediateQueue() {
        return incomingImmediateQueue.peek();
    }

    /**
     * Serves the element at the front of the incoming queue for immediate processing and returns
     * it. The element is subsequently removed from the queue.
     * @return The element at the front of the queue.
     */
    public UnblindDataObject serveElementFromIncomingImmediateQueue() {
        Log.d(TAG, "serving incoming element from immediate queue");
        return incomingImmediateQueue.remove();
    }

    /**
     * Checks if the incoming queue for batch icon processing is empty.
     * @return true if the incoming queue for batch icons is empty, false otherwise.
     */
    public boolean checkIncomingBatchQueueEmpty() {
        return incomingBatchQueue.isEmpty();
    }

    /**
     * Pushes an element to the back of the incoming queue for batch icon processing.
     * @param element Element to be pushed to the back of the queue.
     */
    public void pushElementToIncomingBatchQueue(UnblindDataObject element) {
        incomingBatchQueue.add(element);
        Log.d(TAG, "adding incoming element to batch queue");
    }

    /**
     * Serves an element from the front of the incoming queue for batch icon processing and returns
     * it. Subsequently, the element is removed from the queue.
     * @return Element at the front of the queue.
     */
    public UnblindDataObject serveElementFromIncomingBatchQueue() {
        return incomingBatchQueue.remove();
    }

    /**
     * Pushes an element to the back of the outgoing queue for immediate icon processing.
     * @param element Element to be pushed to into the queue.
     */
    public void pushElementToOutgoingImmediateQueue(UnblindDataObject element) {
        outgoingQueue.add(element);
        Log.d(TAG, "adding outgoing element");
    }

    /**
     * Peeks at the element at the front of the outgoing queue for immediate icon processing.
     * @return Element at the front of the queue.
     */
    public UnblindDataObject getElementFromOutgoingImmediateQueue() {
        return outgoingQueue.peek();
    }

    /**
     * Serves the element at the front of the outgoing queue for immediate icon processing and
     * return it. Subsequently, the item is removed from the queue.
     * @return Element at the front of the queue.
     */
    public UnblindDataObject serveElementFromOutgoingImmediateQueue() {
        return outgoingQueue.remove();
    }

    /**
     * Checks if the outgoing queue for batch icon processing is empty.
     * @return true if the queue is empty, false otherwise.
     */
    public boolean checkOutgoingImmediateQueueEmpty() {
        return outgoingQueue.isEmpty();
    }

}
