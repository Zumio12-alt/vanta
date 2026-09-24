package com.vanta.codm.input;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public final class GestureAccessibility extends AccessibilityService {

    private static final String TAG = "VANTA-ACC";
    private static volatile GestureAccessibility sInstance;

    public static GestureAccessibility get() { return sInstance; }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        Log.i(TAG, "accessibility connected");
    }

    @Override
    public void onDestroy() {
        sInstance = null;
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) { }

    @Override
    public void onInterrupt() { }

    // drag from (sx,sy) to (ex,ey) over duration ms
    public boolean swipe(float sx, float sy, float ex, float ey, long ms) {
        if (Build.VERSION.SDK_INT < 24) return false;
        if (sInstance == null) return false;
        try {
            Path p = new Path();
            p.moveTo(sx, sy);
            p.lineTo(ex, ey);
            GestureDescription.StrokeDescription s =
                new GestureDescription.StrokeDescription(p, 0, Math.max(16, ms));
            GestureDescription g = new GestureDescription.Builder().addStroke(s).build();
            return dispatchGesture(g, null, null);
        } catch (Throwable t) {
            return false;
        }
    }
}
