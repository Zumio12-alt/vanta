package com.vanta.codm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.vanta.codm.ui.PanelView;

public class OverlayService extends Service {

    private static final String TAG = "VANTA-OVERLAY";

    private static PanelView activeInstance;

    private WindowManager wm;
    private PanelView panel;

    public static void showExisting() {
        if (activeInstance != null) {
            activeInstance.post(() -> activeInstance.setVisibility(View.VISIBLE));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForegroundInternal();
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        if (panel == null) {
            attach();
        } else {
            panel.setVisibility(View.VISIBLE);
        }
        return START_STICKY;
    }

    private void attach() {
        panel = new PanelView(this);
        activeInstance = panel;

        int type = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 20;
        lp.y = 120;

        try {
            wm.addView(panel, lp);
            Log.i(TAG, "panel attached");
        } catch (Throwable t) {
            Log.e(TAG, "addView failed", t);
        }
    }

    private void startForegroundInternal() {
        String ch = "vanta";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(new NotificationChannel(
                    ch, "vanta", NotificationManager.IMPORTANCE_LOW));
            }
        }
        Notification n = (Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, ch)
            : new Notification.Builder(this))
            .setContentTitle("VANTA")
            .setContentText("running")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build();
        startForeground(1, n);
    }

    @Override
    public void onDestroy() {
        if (panel != null) {
            try { wm.removeView(panel); } catch (Throwable ignored) {}
            panel = null;
            activeInstance = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
