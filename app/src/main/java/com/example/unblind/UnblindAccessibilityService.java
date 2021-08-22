package com.example.unblind;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
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
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UnblindAccessibilityService extends AccessibilityService implements ColleagueInterface {
    private static final String TAG = "UnBlind AS";
    private AccessibilityManager manager;
    DatabaseService mService;
    private boolean mBound = false;
    private UnblindMediator mediator;
    private Pair<Bitmap, String> currentElement = new Pair(null, "");
    private TextToSpeech tts;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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

        // Get a Base64 encoded PNG of the button bitmap
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        buttonBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Log the PNG
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.v(TAG, "Screenshot result for button: " + encoded);
        return buttonBitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void announceTextFromEvent(String text) {
        tts.speak(text, 1, null,null);
        //        if (manager.isEnabled()) {
//            AccessibilityEvent e = AccessibilityEvent.obtain();
//            e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
//            e.setClassName(getClass().getName());
//            e.getText().add(text);
//            manager.sendAccessibilityEvent(e);
//            Log.e(TAG, "No description found. Custom description added here");
//        }
//        else {
//            Log.e(TAG, "For some reason the manager did not work");
//        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void announceTextFromEvent(String text, AccessibilityEvent event) {
//        if (manager.isEnabled()) {
//            AccessibilityEvent e = AccessibilityEvent.obtain();
//            e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
//            e.setClassName(getClass().getName());
//            e.setPackageName(event.getPackageName());
//            e.getText().add(text);
//            manager.sendAccessibilityEvent(e);
//            Log.e(TAG, "No description found. Custom description added here");
//        }
//        else {
//            Log.e(TAG, "For some reason the manager did not work");
//        }
//    }

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
            }
            else if (event.getText().size() != 0) {
                Log.e(TAG, "event text: " + event.getText());
            }
            else if (event.getContentDescription() != null) {
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
        tts.speak("Processing labels", 2, null,null);

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
                Log.e(TAG, "setting on mediator");
                if (mBound) {
                    mediator.setElement(new Pair<Bitmap, String>(buttonImage, "message"));
                }
                source.recycle();
                // TODO: Send buttonImage to backend for processing here
                // TODO: 'Speak' the returned text/description for buttonImage
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
                }
            }
        });
    }

    @Override
    public void update() {
        System.out.println(currentElement);
        System.out.println(mediator.getElement());
        if (!currentElement.second.equals(mediator.getElement().second)) {
            currentElement = mediator.getElement();
            Log.e(TAG, "updating on accessibility element");
            Log.e(TAG, currentElement.second);
            // currentElement is now complete, can be sent to TalkBack
            announceTextFromEvent(currentElement.second);
        }


    }
}