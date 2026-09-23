package com.vanta.codm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {

    private static final String TAG = "VANTA";
    private WindowManager wm;
    private TextView view;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "OverlayService onCreate");
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForegroundInternal();
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        if (view == null) attach();
        return START_STICKY;
    }

    private void attach() {
        view = new TextView(this);
        view.setText("VANTA ACTIVE");
        view.setTextColor(Color.WHITE);
        view.setBackgroundColor(0xCC000000);
        int p = (int)(16 * getResources().getDisplayMetrics().density);
        view.setPadding(p, p, p, p);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 100;
        lp.y = 200;

        wm.addView(view, lp);
        Log.i(TAG, "overlay view attached");
    }

    private void startForegroundInternal() {
        String ch = "vanta";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(new NotificationChannel(
                ch, "vanta", NotificationManager.IMPORTANCE_LOW));
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
        if (view != null) {
            try { wm.removeView(view); } catch (Throwable ignored) {}
            view = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
