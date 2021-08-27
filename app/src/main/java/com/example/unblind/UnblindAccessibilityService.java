package com.example.unblind;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private Pair<Bitmap, String> currentElement = new Pair(null, "");

    private void setMediator(UnblindMediator mediator) {
        this.mediator = mediator;
        mediator.addObserver(this);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void announceTextFromEvent(String text) {
        if (manager.isEnabled()) {
            AccessibilityEvent e = AccessibilityEvent.obtain();
            e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            e.setClassName(getClass().getName());
            e.getText().add(text);
            manager.sendAccessibilityEvent(e);
            Log.e(TAG, "No description found. Custom description added here");
        } else {
            Log.e(TAG, "For some reason the manager did not work");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG, "onAccessibilityEvent: " + event.getClass().getName());

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        if (source.getText() != null || source.getContentDescription() != null || event.getText().size() != 0) {
            Log.e(TAG, "Existing description found");
            if (source.getText() != null) {
                Log.e(TAG, "source text: " + source.getText());
            } else if (event.getText().size() != 0) {
                Log.e(TAG, "event text: " + event.getText());
            } else if (event.getContentDescription() != null) {
                Log.e(TAG, "content description: " + event.getContentDescription());
            }
            return;
        }
        String currentNodeClassName = (String) source.getClassName();

        if (currentNodeClassName == null || !currentNodeClassName.equals("android.widget.ImageButton")) {
            Log.v(TAG, "currentNodeClassName is not android.widget.ImageButton: " + currentNodeClassName);
            source.recycle();
            return;
        }


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
                byte[] base64EncodedBitmap = UnblindMediator.bitmapToBytes(buttonImage);
                String storedLabel = null;
                if (mBound) {
                    Log.v(TAG, "Checking SP");
                    storedLabel = mService.getSharedData(UnblindMediator.TAG, base64EncodedBitmap);
                }

                // if the label already exists, don't notify
                if (storedLabel != null) {
                    Log.v(TAG, "Found in SP");
                    mediator.pushElementToOutgoing(new Pair<Bitmap, String>(buttonImage, storedLabel));
                    update();
                }

                // else if the label hasn't been seen before, notify
                else if (mBound) {
                    Log.e(TAG, "setting on mediator");
                    mediator.pushElementToIncoming(new Pair<Bitmap, String>(buttonImage, "message"));
                    currentElement = mediator.getElementFromIncoming();
                    if (!mediator.checkIncomingSizeMoreThanOne()) {
                        mediator.notifyObservers();
                    }
                }
                source.recycle();
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
    }

    @Override
    public void update() {
        // Update mediator if the out queue is not empty AND the outgoing element is not the same as the current element?
        if (!mediator.checkOutgoingEmpty() && !currentElement.second.equals(mediator.getElementFromOutgoing().second)) {
            System.out.println(currentElement);
            System.out.println(mediator.getElementFromOutgoing());
            currentElement = mediator.serveElementFromOutgoing();
            Log.e(TAG, "updating on accessibility element");
            Log.e(TAG, currentElement.second);
            // currentElement is now complete, can be sent to TalkBack
            announceTextFromEvent(currentElement.second);
            // if the in queue is not empty, notify observers
            if (!mediator.checkIncomingEmpty()) {
                mediator.notifyObservers();
            }
        }


    }
}
