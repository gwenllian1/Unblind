package com.example.unblind;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    ModelService batchService;
    private AccessibilityManager manager;
    private boolean dbBound = false;
    private boolean modelBound = false;
    private boolean batchBound = false;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextToSpeech tts;
    private boolean ttsReady = false;

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
    private final ServiceConnection batchConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ModelService.LocalBinder binder = (ModelService.LocalBinder) service;
            batchService = binder.getService();
            batchBound = true;
            Log.d(TAG, "batchServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "batchServiceDisconnected");
            batchBound = false;
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

    /**
     * Provide this service a reference to the mediator
     * @param mediator object to aid with transport of data to the model
     */
    private void setMediator(UnblindMediator mediator) {
        this.mediator = mediator;
        mediator.addObserver(this);
    }

    /**
     * Determines if a node is of the correct type to be processed by the service or not
     * @param source The AccessibilityEvent source
     * @return true if the node should be ignored, false otherwise
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
     * Checks if an accessibility node has an existing description that can be read by a screen
     * reader
     * @param source The AccessibilityEvent source
     * @return true if an existing description is found, false otherwise
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
     * Crops the image of an AccessibilityNode of the buttonNode class from a screenshot
     * @param buttonNode the AccessibilityNode whose image will be cropped
     * @param screenShotBM the screenshot that the buttonImage image will be cropped from
     * @return The bitmap of the cropped buttomImage
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
     *
     * @param source
     * @return
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
     *
     * @param node
     * @return
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
     * Batch processing of elements (icons) on screen to be sent to the model on a separate thread
     * @param source source of window content
     * @param screenshot icon screenshot (bitmap)
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
     * Check if icon is present in cache (SharedPreferences)
     * @param buttonImage Bitmap image of the icon
     * @return null if it is not in the cache, the icon description if it is found
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
     * Utilises text to speech engine to read text input, followed by "Double Tap to activate"
     * @param text The text to be read aloud
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void announceTextFromEvent(String text) {
        if (ttsReady) {
            tts.speak(text, 1, null, null);
            tts.speak("Double Tap to activate", 1, null, null);
        }
    }

    /**
     * Called when an accessibility event is detected from user actions
     * @param event A new event
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

        if (ttsReady)
            tts.speak(" ", 2, null, null);

        // From this point, we can assume the source UI element is an image button
        // which has been clicked/tapped
        takeScreenshot(Display.DEFAULT_DISPLAY, threadPoolExecutor, new TakeScreenshotCallback() {
            @Override
            public void onSuccess(@NonNull ScreenshotResult screenshot) {
                Log.v(TAG, "Screenshot successfully taken");
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
                    if (ttsReady)
                        tts.speak("Processing labels", 2, null, null);
                    Log.d(TAG, "setting on mediator");
                    mediator.pushElementToIncomingImmediateQueue(new UnblindDataObject(buttonImage, "", false));
                    // TODO:test if this if condition is needed
                    if (!mediator.checkIncomingImmediateQueueSizeMoreThanOne()) {
                        mediator.notifyObservers();
                    }
                }
                batchProcess(getHighestParent(source), screenShotBM);
                source.recycle();
                return;
            }

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
     * Called to interrupt the Accessibility feedback
     */
    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong");
    }

    /**
     * Called when connection to service is established
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
        bindService(batchIntent, batchConnection, Context.BIND_AUTO_CREATE);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("Test", "123: " + status);
                if (status != TextToSpeech.ERROR) {
                    Log.d("Test", "123");
                    // Setting locale, speech rate and voice pitch
                    tts.setLanguage(Locale.UK);
                    tts.setSpeechRate(1.0f);
                    tts.setPitch(1.0f);
                    ttsReady = true;
                }
            }
        });
    }

    /**
     * Performs final cleanup before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        unbindService(modelConnection);
        unbindService(batchConnection);
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

        if (currentElement.batchStatus) {
            Log.v(TAG, "Received generated batch label: " + currentElement.iconLabel);
            Log.v(TAG, "Not speaking batch label...");
            return;
        }
        Log.d(TAG, currentElement.iconLabel);
        // currentElement is now complete, can be sent to TalkBack
        announceTextFromEvent(currentElement.iconLabel);
        // if the in queue is not empty, notify observers
        if (!mediator.checkIncomingImmediateQueueEmpty()) {
            mediator.notifyObservers();
        }
    }
}