package com.vanta.codm.vision;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.vanta.codm.input.GestureAccessibility;

import java.nio.ByteBuffer;

public class VisionService extends Service {

    private static final String TAG = "VANTA-VISION";
    public static final String EXTRA_RESULT_CODE = "rc";
    public static final String EXTRA_RESULT_DATA = "rd";

    public static volatile boolean aimEnabled  = false;
    public static volatile float   aimFovPx    = 240f;
    public static volatile float   aimStrength = 0.25f;

    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private final Detector detector = new Detector();
    private final Handler ui = new Handler(Looper.getMainLooper());

    private int screenW, screenH, density;
    private long lastTick;

    private void toast(final String msg) {
        Log.i(TAG, msg);
        ui.post(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        toast("V1 service started");

        if (projection != null) {
            toast("V2 already running");
            try { postForeground(); } catch (Throwable t) { toast("V2err " + t.getMessage()); }
            return START_STICKY;
        }

        try {
            int rc = (intent == null) ? 0 : intent.getIntExtra(EXTRA_RESULT_CODE, 0);
            Intent rd = (intent == null) ? null : intent.getParcelableExtra(EXTRA_RESULT_DATA);

            if (rc == 0) { toast("V3 no result code"); stopSelf(); return START_NOT_STICKY; }
            if (rd == null) { toast("V4 no result data"); stopSelf(); return START_NOT_STICKY; }

            MediaProjectionManager mpm = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mpm == null) { toast("V5 no mpm"); stopSelf(); return START_NOT_STICKY; }

            projection = mpm.getMediaProjection(rc, rd);
            if (projection == null) { toast("V6 projection null"); stopSelf(); return START_NOT_STICKY; }
            toast("V7 projection obtained");

            try {
                postForeground();
                toast("V8 foreground ok");
            } catch (Throwable t) {
                toast("V8err " + t.getClass().getSimpleName() + " " + t.getMessage());
                stopSelf();
                return START_NOT_STICKY;
            }

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(dm);
            screenW = dm.widthPixels;
            screenH = dm.heightPixels;
            density = dm.densityDpi;

            reader = ImageReader.newInstance(screenW, screenH, PixelFormat.RGBA_8888, 2);
            reader.setOnImageAvailableListener(this::onFrame, ui);

            display = projection.createVirtualDisplay(
                "vanta-vision",
                screenW, screenH, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.getSurface(), null, ui);

            toast("V9 capture " + screenW + "x" + screenH);
            return START_STICKY;

        } catch (Throwable t) {
            toast("VX " + t.getClass().getSimpleName() + ": " + t.getMessage());
            Log.e(TAG, "onStartCommand failed", t);
            try { stopSelf(); } catch (Throwable ignored) {}
            return START_NOT_STICKY;
        }
    }

    private void postForeground() {
        String ch = "vanta-vision";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(new NotificationChannel(
                    ch, "vision", NotificationManager.IMPORTANCE_LOW));
            }
        }
        Notification n = (Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, ch)
            : new Notification.Builder(this))
            .setContentTitle("VANTA vision")
            .setContentText("screen capture active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(2, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(2, n);
        }
    }

    private void onFrame(ImageReader r) {
        Image img = null;
        try { img = r.acquireLatestImage(); } catch (Throwable t) { return; }
        if (img == null) return;

        long now = System.currentTimeMillis();
        if (!aimEnabled || now - lastTick < 33) {
            img.close();
            return;
        }
        lastTick = now;

        try {
            Image.Plane[] planes = img.getPlanes();
            ByteBuffer buf = planes[0].getBuffer();
            int rowStride = planes[0].getRowStride();
            int pxStride  = planes[0].getPixelStride();

            byte[] all = new byte[screenW * screenH * 4];
            for (int y = 0; y < screenH; ++y) {
                int rowBase = y * rowStride;
                int outBase = y * screenW * 4;
                for (int x = 0; x < screenW; ++x) {
                    int off = outBase + x * 4;
                    int src = rowBase + x * pxStride;
                    all[off]     = buf.get(src + 3);
                    all[off + 1] = buf.get(src);
                    all[off + 2] = buf.get(src + 1);
                    all[off + 3] = buf.get(src + 2);
                }
            }

            Bitmap bmp = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(ByteBuffer.wrap(all));

            float cx = screenW * 0.5f;
            float cy = screenH * 0.5f;
            int fov = (int) aimFovPx;
            Rect region = new Rect(
                (int)(cx - fov), (int)(cy - fov),
                (int)(cx + fov), (int)(cy + fov));

            Detector.Hit hit = detector.findTarget(bmp, region, cx, cy);
            bmp.recycle();

            if (hit == null) return;

            GestureAccessibility svc = GestureAccessibility.get();
            if (svc == null) return;

            float dx = (hit.x - cx) * aimStrength;
            float dy = (hit.y - cy) * aimStrength;

            float maxMove = 90f;
            if (dx >  maxMove) dx =  maxMove;
            if (dx < -maxMove) dx = -maxMove;
            if (dy >  maxMove) dy =  maxMove;
            if (dy < -maxMove) dy = -maxMove;

            if (Math.abs(dx) < 1f && Math.abs(dy) < 1f) return;

            svc.swipe(cx, cy, cx + dx, cy + dy, 16);

        } catch (Throwable t) {
            // skip frame
        } finally {
            img.close();
        }
    }

    @Override
    public void onDestroy() {
        if (display != null) { display.release(); display = null; }
        if (projection != null) { projection.stop(); projection = null; }
        if (reader != null) { reader.close(); reader = null; }
        Log.i(TAG, "service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
