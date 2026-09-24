package com.vanta.codm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vanta.codm.vision.VisionService;

public class MainActivity extends Activity {

    private static final String TAG = "VANTA-MAIN";
    private static final int REQ_OVERLAY = 0x4d4b;
    private static final int REQ_CAPTURE = 0x4d4c;

    private static final int TILE_BLUE       = 0xFF0A6CFF;
    private static final int TILE_RED        = 0xFFE51427;
    private static final int TILE_TEAL       = 0xFF2DC9B9;
    private static final int TILE_PERIWINKLE = 0xFF7A80E0;
    private static final int TILE_DIM        = 0xFF232838;
    private static final int TILE_AMBER      = 0xFFF5A623;
    private static final int TEXT_PRIMARY    = 0xFFFFFFFF;
    private static final int AMBER           = 0xFFF5A623;

    private static final String[] CONTAINER_PKGS = {
        "com.amy.virtual", "com.lbe.parallel.intl", "com.lbe.parallel",
        "com.excean.multiple", "com.excean.gspace",
        "com.clone.android.dual.space", "com.polestar.domultiple",
        "com.dualspace.multiapp", "com.x8.sandbox",
        "io.f1vm.f1vm", "com.waxmoon.ma.gp",
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(20), dp(36), dp(20), dp(24));

        TextView avatar = new TextView(this);
        avatar.setText("V");
        avatar.setTextColor(TEXT_PRIMARY);
        avatar.setTextSize(28);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(0xFF1B2029);
        circle.setStroke(dp(2), 0x22FFFFFF);
        avatar.setBackground(circle);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(64), dp(64));
        avLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(avatar, avLp);

        String lic = new Preferences(this).getStr("license", "-");
        TextView expiry = new TextView(this);
        expiry.setText("License: " + lic);
        expiry.setTextColor(AMBER);
        expiry.setTextSize(13);
        expiry.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams exLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        exLp.topMargin = dp(8);
        exLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(expiry, exLp);

        LinearLayout rowA = new LinearLayout(this);
        rowA.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowALp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(140));
        rowALp.topMargin = dp(22);
        root.addView(rowA, rowALp);

        View start = buildTile("▶", "START OVERLAY", TILE_BLUE);
        LinearLayout.LayoutParams tLp1 = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        tLp1.rightMargin = dp(10);
        rowA.addView(start, tLp1);
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            } else {
                launchOverlay();
                Toast.makeText(this, "overlay on", Toast.LENGTH_SHORT).show();
            }
        });

        View stop = buildTile("❚❚", "STOP OVERLAY", TILE_RED);
        LinearLayout.LayoutParams tLp2 = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        tLp2.leftMargin = dp(10);
        rowA.addView(stop, tLp2);
        stop.setOnClickListener(v ->
            stopService(new Intent(this, OverlayService.class)));

        View acc = buildTile("♿", "ENABLE ACCESSIBILITY", TILE_AMBER);
        LinearLayout.LayoutParams accLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(66));
        accLp.topMargin = dp(14);
        root.addView(acc, accLp);
        acc.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                Toast.makeText(this, "enable VANTA in the list", Toast.LENGTH_LONG).show();
            } catch (Throwable t) {
                Toast.makeText(this, "open Settings manually", Toast.LENGTH_LONG).show();
            }
        });

        View cap = buildTile("◉", "GRANT VISION (screen capture)", TILE_TEAL);
        LinearLayout.LayoutParams capLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(66));
        capLp.topMargin = dp(10);
        root.addView(cap, capLp);
        cap.setOnClickListener(v -> {
            Log.i(TAG, "GRANT VISION tapped");
            MediaProjectionManager mpm = (MediaProjectionManager)
                getSystemService(MEDIA_PROJECTION_SERVICE);
            if (mpm == null) {
                Toast.makeText(this, "no MediaProjectionManager", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Intent captureIntent = mpm.createScreenCaptureIntent();
                Log.i(TAG, "starting capture intent");
                startActivityForResult(captureIntent, REQ_CAPTURE);
            } catch (Throwable t) {
                Log.e(TAG, "createScreenCaptureIntent failed", t);
                Toast.makeText(this, "capture intent failed: " + t.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });

        View run = buildTile("▶", "Open Clone App", TILE_DIM);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(66));
        runLp.topMargin = dp(10);
        root.addView(run, runLp);
        run.setOnClickListener(v -> launchContainer());

        LinearLayout comm = new LinearLayout(this);
        comm.setOrientation(LinearLayout.HORIZONTAL);
        comm.setGravity(Gravity.CENTER_VERTICAL);
        comm.setPadding(dp(18), 0, dp(18), 0);
        GradientDrawable commBg = new GradientDrawable();
        commBg.setColor(TILE_PERIWINKLE);
        commBg.setCornerRadius(dp(14));
        comm.setBackground(commBg);
        LinearLayout.LayoutParams commLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(72));
        commLp.topMargin = dp(14);
        root.addView(comm, commLp);

        TextView tg = new TextView(this);
        tg.setText("➤");
        tg.setTextColor(0xFFFF3B3B);
        tg.setTextSize(26);
        LinearLayout.LayoutParams tgLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        tgLp.rightMargin = dp(14);
        comm.addView(tg, tgLp);

        LinearLayout commCol = new LinearLayout(this);
        commCol.setOrientation(LinearLayout.VERTICAL);
        comm.addView(commCol, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView commTitle = new TextView(this);
        commTitle.setText("Join Community Server");
        commTitle.setTextColor(TEXT_PRIMARY);
        commTitle.setTextSize(14);
        commTitle.setTypeface(null, Typeface.BOLD);
        commCol.addView(commTitle);

        TextView commSub = new TextView(this);
        commSub.setText("To ask, give feedback, get updates");
        commSub.setTextColor(0xDDE5F5FF);
        commSub.setTextSize(10);
        commCol.addView(commSub);

        comm.setOnClickListener(v -> startActivity(new Intent(
            Intent.ACTION_VIEW, Uri.parse("https://t.me/VantaDisini"))));

        LinearLayout logout = new LinearLayout(this);
        logout.setOrientation(LinearLayout.HORIZONTAL);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(dp(28), 0, dp(28), 0);
        GradientDrawable loBg = new GradientDrawable();
        loBg.setColor(TILE_RED);
        loBg.setCornerRadius(dp(48));
        logout.setBackground(loBg);
        LinearLayout.LayoutParams loLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(48));
        loLp.topMargin = dp(22);
        loLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logout, loLp);

        TextView loText = new TextView(this);
        loText.setText("LogOut");
        loText.setTextColor(TEXT_PRIMARY);
        loText.setTextSize(13);
        loText.setTypeface(null, Typeface.BOLD);
        logout.addView(loText);

        logout.setOnClickListener(v -> {
            new Preferences(this).putStr("license", "");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setContentView(root);
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);
        Log.i(TAG, "onActivityResult req=" + r + " result=" + c + " data=" + (d != null));

        if (r == REQ_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                launchOverlay();
            } else {
                Toast.makeText(this, "overlay permission denied",
                    Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (r == REQ_CAPTURE) {
            if (c != RESULT_OK) {
                Toast.makeText(this, "capture cancelled (code " + c + ")",
                    Toast.LENGTH_LONG).show();
                return;
            }
            if (d == null) {
                Toast.makeText(this, "capture: no data returned",
                    Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "starting vision service...", Toast.LENGTH_SHORT).show();
            try {
                Intent i = new Intent(this, VisionService.class);
                i.putExtra(VisionService.EXTRA_RESULT_CODE, c);
                i.putExtra(VisionService.EXTRA_RESULT_DATA, d);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(i);
                    Log.i(TAG, "startForegroundService called");
                } else {
                    startService(i);
                    Log.i(TAG, "startService called");
                }
            } catch (Throwable t) {
                Log.e(TAG, "start VisionService failed", t);
                Toast.makeText(this, "start failed: " + t.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void launchOverlay() {
        Intent i = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }

    private void launchContainer() {
        for (String pkg : CONTAINER_PKGS) {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            }
        }
        Toast.makeText(this, "no clone app found", Toast.LENGTH_SHORT).show();
    }

    private View buildTile(String icon, String label, int color) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.HORIZONTAL);
        tile.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(14));
        tile.setBackground(bg);

        TextView ico = new TextView(this);
        ico.setText(icon);
        ico.setTextColor(TEXT_PRIMARY);
        ico.setTextSize(26);
        ico.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams icoLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        icoLp.rightMargin = dp(12);
        tile.addView(ico, icoLp);

        TextView txt = new TextView(this);
        txt.setText(label);
        txt.setTextColor(TEXT_PRIMARY);
        txt.setTextSize(13);
        txt.setLetterSpacing(0.08f);
        txt.setTypeface(null, Typeface.BOLD);
        tile.addView(txt);

        return tile;
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }
}
