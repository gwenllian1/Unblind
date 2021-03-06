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

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A service that initiates the workflow of the Unblind application.
 * On-screen accessibility elements are accessed through screenshots of this service and the icons
 * are passed to the mediator for processing.
 */
public class UnblindAccessibilityService extends AccessibilityService implements ColleagueInterface {
    private static final String TAG = "UnBlind AS";
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    DatabaseService databaseService;
    ModelService modelService;
    private AccessibilityManager manager;
    private AccessibilityNodeInfo clickedNode = new AccessibilityNodeInfo();
    private boolean dbBound = false;
    private boolean modelBound = false;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ServiceConnection modelConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ModelService.LocalBinder binder = (ModelService.LocalBinder) service;
            modelService = binder.getService();
            modelBound = true;
            Log.d(TAG, "modelServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "modelServiceDisconnected");
            modelBound = false;
        }
    };
    private UnblindMediator mediator;
    private final ServiceConnection dbConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DatabaseService.LocalBinder binder = (DatabaseService.LocalBinder) service;
            databaseService = binder.getService();
            dbBound = true;
            setMediator(databaseService.getUnblindMediator());
            Log.d(TAG, "databaseServiceConnected");

        }


        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "databaseServiceDisconnected");
            dbBound = false;
        }
    };
    private UnblindDataObject currentElement = new UnblindDataObject(null, "", true);
    private UnblindTextToSpeech defaultTextToSpeech;

    /**
     * This function is a setter for the mediator
     * @param mediator : Unblind Mediator
     */
    private void setMediator(UnblindMediator mediator) {
        this.mediator = mediator;
        mediator.addObserver(this);
    }

    /**
     * This function checks the node class and returns a boolean whether the node is the type in which the model is interested in
     * @param source : the current selected node
     * @return boolean whether the node is the type in which the model is interested in
     */
    private boolean shouldIgnoreNode(AccessibilityNodeInfo source) {
        // Helper method to limit focus to only the specified classes

        String currentNodeClassName = (String) source.getClassName();
        boolean ignoreNode = true;
        if (currentNodeClassName != null) {
            if (currentNodeClassName.equals("android.widget.ImageButton")) {
                ignoreNode = false;
            }
            if (currentNodeClassName.equals("android.widget.ImageView")) {
                ignoreNode = false;
            }
            if (currentNodeClassName.equals("android.widget.FrameLayout")) {
                ignoreNode = false;
            }
        }
        return ignoreNode;
    }

    /**
     * This function checks if the currently selected node has a description
     * @param source : the current selected node
     * @return boolean whether the node has a description
     */
    private boolean nodeHasDescription(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }
        boolean ret = false;
        if (source.getText() != null) {
            Log.v(TAG, "Node getText: " + source.getText());
            ret = true;
        }
        if (source.getContentDescription() != null) {
            Log.v(TAG, "Node getContentDescription: " + source.getContentDescription());
            ret = true;
        }
        if (ret)
            Log.v(TAG, "Existing description found for node");
        return ret;
    }

    /**
     * This function gets button images from a screenshot
     * @param buttonNode : button nodes
     * @param screenShotBM : Screenshot omage of the screen
     * @return : bitmap of the button
     */
    private Bitmap getButtonImageFromScreenshot(AccessibilityNodeInfo buttonNode, Bitmap screenShotBM) {
        Rect rectTest = new Rect();
        // Copy the dimensions of the button to the rectTest object
        //  This check could be moved to the calling 'screenshot success' function
        buttonNode.getBoundsInScreen(rectTest);
        if (rectTest.width() < 1 || rectTest.height() < 1) {
            // The LabelDroid model probably can't infer much from a single pixel...
            return screenShotBM; // Probably change this...
        }
        // Crop the relevant portion of the screenshot into a new Bitmap
        Bitmap buttonBitmap = Bitmap.createBitmap(screenShotBM, rectTest.left, rectTest.top, rectTest.width(), rectTest.height());

        Log.v(TAG, "Screenshot encoded");
        return buttonBitmap;
    }

    /**
     * This function finds the highest parent node that wraps all nodes from the current selected node
     * @param source the currently selected node
     * @return :the highest parent node that wraps all the nodes
     */
    private AccessibilityNodeInfo getHighestParent(AccessibilityNodeInfo source) {
        Log.v(TAG, "Finding highest parent of initial node in batchProcess");
        AccessibilityNodeInfo tempNode = source.getParent();
        AccessibilityNodeInfo returnNode = source;
        while (tempNode != null) {
            Log.v(TAG, "Still finding highest parent of initial node in batchProcess...");
            returnNode = tempNode;
            tempNode = returnNode.getParent();
        }
        return returnNode;
    }

    /**
     * Checks if a node has children which can be classified by the model
     * @param node a node to check if there are any iconbutton children
     * @return true if there are iconbutton children, false otherwise
     */
    private boolean nodeHasRelevantChildren(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }

        if (!shouldIgnoreNode(node) && !nodeHasDescription(node)) {
            Log.v(TAG, "Node is a relevant type and has no description");
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            boolean temp = nodeHasRelevantChildren(node.getChild(i));
            if (temp) {
                Log.v(TAG, "Node has at least one relevant, unlabelled child node");
                return true;
            }
        }
        Log.v(TAG, "Node has no relevant children nodes");
        return false;
    }

    /**
     * This function performs batch processing
     * @param source: AccessibilityNodeInfo, the highest parent node that wraps all the nodes
     * @param screenshot: Screenshot image of the whole screen
     */
    private void batchProcess(AccessibilityNodeInfo source, Bitmap screenshot) {
        if (source == null) {
            return;
        }

        Log.v(TAG, "batchProcess - childCount = " + source.getChildCount());
        if (!shouldIgnoreNode(source)) {
            // To avoid unnecessary usage of the model, below check should be enabled when not testing
            if (nodeHasDescription(source) == false) {
                Log.v(TAG, "Getting bitmap for node in batch");
                Bitmap buttonImage = getButtonImageFromScreenshot(source, screenshot).copy(Bitmap.Config.ARGB_8888, true);

                // Disable cache lookup for now
                String storedLabel = checkIconCache(buttonImage);
                if (storedLabel != null) {
                    Log.v(TAG, "Found cached batch icon: " + storedLabel);
                    //announceTextFromEvent(storedLabel);
                } else if (dbBound) {
                    executorService.execute(() -> {
                        // else if the label hasn't been seen before, notify
                        UnblindDataObject element = new UnblindDataObject(buttonImage, null, true);
                        Log.v(TAG, "batchProcess pushing element to incoming queue : " + element);
                        mediator.pushElementToIncomingBatchQueue(element);
                        Log.v(TAG, "batchProcess finished pushing element to incoming queue : " + element);
                        mediator.notifyObservers();
                    });

                }
            }
        }

        for (int i = 0; i < source.getChildCount(); i++) {
            batchProcess(source.getChild(i), screenshot);
        }
    }

    /**
     * This function checks for icon if it exists within the cache
     * @param buttonImage: Bitmap of the button image
     * @return : the icon label string if it exists
     */
    private String checkIconCache(Bitmap buttonImage) {
        byte[] base64EncodedBitmap = UnblindMediator.bitmapToBytes(buttonImage);
        String storedLabel = null;
        if (dbBound) {
            Log.v(TAG, "Checking SP");
            storedLabel = databaseService.getSharedData(UnblindMediator.TAG, base64EncodedBitmap);
        }
        return storedLabel;
    }

    /**
     * This function speaks out given text strings
     * @param text: string to be translated and spoken
     * @param mode: Queuing strategy, an integer for whether it is blocking (2) or speaking out queue (1)
     */
    private void announceTextFromEvent(String text, int mode) {
        if (!defaultTextToSpeech.isTtsReady()) {
            Log.d(TAG, "Text-to-speech is not available, attempt to reconnect");
            defaultTextToSpeech = new UnblindTextToSpeech(this);
        } else {
            defaultTextToSpeech.ttsSpeak(text, mode, null, null);
        }
    }

    /**
     * This function is invoked whenever an accessibility event of "typeViewAccessibilityFocused" occurs
     * Takes screenshot, crops bitmap of the button node and sends this bitmap to model service
     * @param event: type accessibility event: typeViewAccessibilityFocused
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG, "onAccessibilityEvent: " + event.getClass().getName());

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        if (nodeHasDescription(source)) {
            source.recycle();
            return;
        }

        if (shouldIgnoreNode(source)) {
            // We don't try to describe non-buttons or basic image views at this point
            source.recycle();
            return;
        }
        clickedNode = source;
        announceTextFromEvent(" ", 2);
        defaultTextToSpeech.updateTTSConfig(getApplicationContext());
        announceTextFromEvent(" ", 2);

        // From this point, we can assume the source UI element is an image button
        // which has been clicked/tapped
        takeScreenshot(Display.DEFAULT_DISPLAY, threadPoolExecutor, new TakeScreenshotCallback() {
            /**
             * This function invokes if the screenshot function executes successfully. It checks if the icon label is within the cache,
             * and if it is then it reads it out, if not it updates the new icon label to the cache
             * @param screenshot: Screenshot image of the whole screen
             */
            @Override
            public void onSuccess(@NonNull ScreenshotResult screenshot) {
                Log.v(TAG, "Screenshot successfully taken");
                // Converting screenshot to BitMap
                final HardwareBuffer hardwareBuffer = screenshot.getHardwareBuffer();
                final ColorSpace colorSpace = screenshot.getColorSpace();
                Bitmap screenShotBM = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace);
                hardwareBuffer.close();
                Bitmap buttonImage = getButtonImageFromScreenshot(source, screenShotBM).copy(Bitmap.Config.ARGB_8888, true);

                // check screenshot against storage before notifying
                String storedLabel = checkIconCache(buttonImage);

                // if the label already exists, don't notify
                if (storedLabel != null) {
                    Log.v(TAG, "Found in SP");
                    mediator.pushElementToOutgoingImmediateQueue(new UnblindDataObject(buttonImage, storedLabel, false));
                    update();
                } else if (dbBound) {
                    // else if the label hasn't been seen before, notify
                    announceTextFromEvent(" ", 2);
                    Log.e(TAG, "setting on mediator");
                    mediator.pushElementToIncomingImmediateQueue(new UnblindDataObject(buttonImage, "", false));
                    currentElement = mediator.getElementFromIncomingImmediateQueue();
                    // Current Element has action upon clicking
                    if(source.isClickable() || source.isLongClickable()){
                        currentElement.isClickable = true;
                    }
                    if (!mediator.checkIncomingImmediateQueueSizeMoreThanOne()) {
                        mediator.notifyObservers();
                    }
                }
                // Passing all the nodes into Batch Process
                batchProcess(getHighestParent(source), screenShotBM);
                source.recycle();
                return;
            }

            /**
             * This function is an error handler, called if screenshot taken is not successful,
             * if screenshot is taken too fast or too often repetitively
             * @param errorCode: integer of the error
             */
            @Override
            public void onFailure(int errorCode) {
                Log.e(TAG, "Failed to take screenshot - errorCode: " + errorCode);
                source.recycle();
                // At this stage, this is probably an issue for our purposes
                // This can occur due to the rate-limiting Android enforces on screenshots
            }
        });
    }

    /**
     * Function to display error handling, to display when system is interrupted/ abruptly disconnected
     */
    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong");
    }

    /**
     * This Function initialises the set up of the Service, including the database, the model,
     * Text to Speech and any other related services
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        manager = (AccessibilityManager)
                getSystemService(Context.ACCESSIBILITY_SERVICE);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED;

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");

        // Bind DatabaseService
        Intent dbIntent = new Intent(this, DatabaseService.class);
//        startService(dbIntent);
        bindService(dbIntent, dbConnection, Context.BIND_AUTO_CREATE);

        // Bind ModelService
        Intent modelIntent = new Intent(this, ModelService.class);
        bindService(modelIntent, modelConnection, Context.BIND_AUTO_CREATE);

        // Bind BatchService
        Intent batchIntent = new Intent(this, ModelService.class);
        defaultTextToSpeech = new UnblindTextToSpeech(this);
    }

    /**
     * This function turns off all services to avoid crashing and information leak
     */
    @Override
    public void onDestroy() {
        unbindService(modelConnection);
        unbindService(dbConnection);
    }

    /**
     * Updates relevant elements when changes are observed. Called by the mediator when changes
     * occur.
     * Announces text from given description by the model (through the mediator)
     */
    @Override
    public void update() {
        // Update mediator if the out queue is not empty AND the outgoing element is not the same as the current element?
        Log.v(TAG, "Update");

        if (mediator.checkOutgoingImmediateQueueEmpty()) {
            Log.v(TAG, "outgoing queue is empty");
            return;
        }

        if(mediator.getElementFromOutgoingImmediateQueue() == null)
            return;

        UnblindDataObject currentElement = mediator.serveElementFromOutgoingImmediateQueue();
        Log.d(TAG, "updating on accessibility element");

        // Checking if current element is in batch processing
        if (currentElement.batchStatus) {
            Log.v(TAG, "Received generated batch label: " + currentElement.iconLabel);
            Log.v(TAG, "Not speaking batch label...");
            return;
        }
        Log.d(TAG, currentElement.iconLabel);
        // currentElement is now complete, can be sent to TalkBack
        announceTextFromEvent(currentElement.iconLabel, 1);
        if(clickedNode.isClickable() || clickedNode.isLongClickable()){
            announceTextFromEvent( "Double Tap to activate", 1);
        }

        // if the in queue is not empty, notify observers
        if (!mediator.checkIncomingImmediateQueueEmpty()) {
            mediator.notifyObservers();
        }
    }
}