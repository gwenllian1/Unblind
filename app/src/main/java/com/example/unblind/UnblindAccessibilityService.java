package com.example.unblind;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class UnblindAccessibilityService extends AccessibilityService {
    private static final String TAG = "UnblindAccessibilitySer";
    private AccessibilityManager manager;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.e(TAG, "onAccessibilityEvent: " + event.getEventType());

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        if (source.getText() == null && source.getContentDescription() == null && event.getText().size() == 0) {
            if (manager.isEnabled()) {
                AccessibilityEvent e = AccessibilityEvent.obtain();
                e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                e.setClassName(getClass().getName());
                e.setPackageName(event.getPackageName());
                e.getText().add("some textaaa");
                manager.sendAccessibilityEvent(e);
                Log.e(TAG, "No description found. Custom description added here");
            }
            else {
                Log.e(TAG, "For some reason the manager did not work");
            }
        }
        else {
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
        }

        source.recycle();

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
    }
}
