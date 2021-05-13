package com.example.unblind;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UnblindAccessibilityService extends AccessibilityService {
    private static final String TAG = "UnblindAccessibilitySer";

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: " + event.getClass().getName());

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        if (event.getText() == null) {
            Log.e(TAG, "event text " + event.getText());
        } else {
            Log.e(TAG, "evnet text: none");
        }
        
        if (source.getContentDescription() == null) {
            Log.e(TAG, "description: " + "custom added description");
        } else {
            Log.e(TAG, "description: " + source.getContentDescription());
        }

        if (source.getText() == null) {
            Log.e(TAG, "view text: none");
        } else {
            Log.e(TAG, "view text: " + source.getText());
        }
        String currentNodeClassName = (String) source.getClassName();

        if (currentNodeClassName == null || !currentNodeClassName.equals("android.widget.ImageButton")) {
            Log.w("Demo AS - currentNodeClassName is not android.widget.ImageButton: ", currentNodeClassName);
            source.recycle();
            return;
        }

        // From this point, we can assume the source UI element is an image button
        takeScreenshot(Display.DEFAULT_DISPLAY, threadPoolExecutor, new TakeScreenshotCallback() {
            @Override
            public void onSuccess(@NonNull ScreenshotResult screenshot) {
                Log.w("Unblind", "Screenshot successfully taken");
                final HardwareBuffer hardwareBuffer = screenshot.getHardwareBuffer();
                final ColorSpace colorSpace = screenshot.getColorSpace();
                Bitmap screenShotBM = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace);
                Rect rectTest = new Rect();
                source.getBoundsInScreen(rectTest);
                if (rectTest.width() < 1 || rectTest.height() < 1) {
                    source.recycle();
                    return;
                }
                Bitmap cropped = Bitmap.createBitmap(screenShotBM, rectTest.left, rectTest.top, rectTest.width(), rectTest.height());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                cropped.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.w("Screenshot result", encoded);

                source.recycle();
            }

            @Override
            public void onFailure(int errorCode) {
                Log.w("Unblind", "Failed to take screenshot - errorCode:" + errorCode);
                source.recycle();
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

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;


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
    }
}
