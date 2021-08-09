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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UnblindAccessibilityService extends AccessibilityService implements ColleagueInterface {
    private static final String TAG = "UnBlind AS";
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    DatabaseService mService;
    private AccessibilityManager manager;
    private boolean mBound = false;
    private UnblindMediator mediator;
    private UnblindDataObject currentElement = new UnblindDataObject(null, "", true);
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DatabaseService.LocalBinder binder = (DatabaseService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            setMediator(mService.getUnblindMediator());
            Log.e(TAG, "databaseServiceConnected");

        }


        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "databaseServiceDisconnected");
            mBound = false;
        }
    };

    private void setMediator(UnblindMediator mediator) {
        this.mediator = mediator;
        mediator.addObserver(this);
    }

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

    private String checkIconCache(Bitmap buttonImage) {
        byte[] base64EncodedBitmap = UnblindMediator.bitmapToBytes(buttonImage);
        String storedLabel = null;
        if(mBound) {
            Log.v(TAG, "Checking SP");
            storedLabel = mService.getSharedData(UnblindMediator.TAG, base64EncodedBitmap);
        }
        return storedLabel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void announceTextFromEvent(String text) {
        if (ttsReady) {
            tts.speak(text, 1, null, null);
            tts.speak("Double Tap to activate", 1, null, null);
        }
    }

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
            tts.speak(" ", 2, null,null);

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
                    mediator.pushElementToOutgoing(new UnblindDataObject(buttonImage, storedLabel, false));
                    update();
                } else if (mBound) {
                    // else if the label hasn't been seen before, notify
                    if (ttsReady)
                        tts.speak("Processing labels", 2, null,null);
                    Log.e(TAG, "setting on mediator");
                    mediator.pushElementToIncoming(new UnblindDataObject(buttonImage, "", false));
                    currentElement = mediator.getElementFromIncoming();
                    if (!mediator.checkIncomingSizeMoreThanOne()) {
                        mediator.notifyObservers();
                    }
                }
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

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong");
    }

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
        Log.e(TAG, "onServiceConnected: ");

        // Bind DatabaseService
        Intent intent = new Intent(this, DatabaseService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("Test","123: " + status);
                if (status != TextToSpeech.ERROR) {
                    Log.d("Test","123");
                    // Setting locale, speech rate and voice pitch
                    tts.setLanguage(Locale.UK);
                    tts.setSpeechRate(1.0f);
                    tts.setPitch(1.0f);
                    ttsReady = true;
                }
            }
        });
    }

    @Override
    public void update() {
        // Update mediator if the out queue is not empty AND the outgoing element is not the same as the current element?
        Log.v(TAG,"Update");

        if (mediator.checkOutgoingEmpty()) {
            Log.v(TAG, "outgoing queue is empty");
            return;
        }

        if(mediator.getElementFromOutgoing() == null)
            return;

        System.out.println(currentElement);
        System.out.println(mediator.getElementFromOutgoing());
        currentElement = mediator.serveElementFromOutgoing();
        Log.e(TAG, "updating on accessibility element");
        Log.e(TAG, currentElement.iconLabel);
        // currentElement is now complete, can be sent to TalkBack
        announceTextFromEvent(currentElement.iconLabel);
        // if the in queue is not empty, notify observers
        if (!mediator.checkIncomingEmpty()) {
            mediator.notifyObservers();
        }
    }
}