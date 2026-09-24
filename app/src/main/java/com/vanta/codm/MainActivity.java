package com.vanta.codm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 0x4d4b;

    private static final int TILE_BLUE       = 0xFF0A6CFF;
    private static final int TILE_RED        = 0xFFE51427;
    private static final int TILE_TEAL       = 0xFF2DC9B9;
    private static final int TILE_PERIWINKLE = 0xFF7A80E0;
    private static final int TEXT_PRIMARY    = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY  = 0xFFB0B5BF;
    private static final int AMBER           = 0xFFF5A623;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(20), dp(42), dp(20), dp(32));

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
        exLp.topMargin = dp(10);
        exLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(expiry, exLp);

        // two-tile row: START / STOP
        LinearLayout rowA = new LinearLayout(this);
        rowA.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowALp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(160));
        rowALp.topMargin = dp(28);
        root.addView(rowA, rowALp);

        View start = buildTile("▶", "START MENU", TILE_BLUE);
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
            }
        });

        View stop = buildTile("❚❚", "STOP MENU", TILE_RED);
        LinearLayout.LayoutParams tLp2 = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        tLp2.leftMargin = dp(10);
        rowA.addView(stop, tLp2);
        stop.setOnClickListener(v ->
            stopService(new Intent(this, OverlayService.class)));

        // Run The Game
        View run = buildTile("▶", "Run The Game", TILE_TEAL);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(88));
        runLp.topMargin = dp(20);
        root.addView(run, runLp);
        run.setOnClickListener(v -> {
            Intent i = getPackageManager()
                .getLaunchIntentForPackage("com.garena.game.codm");
            if (i == null) i = getPackageManager()
                .getLaunchIntentForPackage("com.activision.callofduty.shooter");
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        // community tile
        LinearLayout comm = new LinearLayout(this);
        comm.setOrientation(LinearLayout.HORIZONTAL);
        comm.setGravity(Gravity.CENTER_VERTICAL);
        comm.setPadding(dp(20), 0, dp(20), 0);
        GradientDrawable commBg = new GradientDrawable();
        commBg.setColor(TILE_PERIWINKLE);
        commBg.setCornerRadius(dp(14));
        comm.setBackground(commBg);
        LinearLayout.LayoutParams commLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(88));
        commLp.topMargin = dp(16);
        root.addView(comm, commLp);

        TextView tg = new TextView(this);
        tg.setText("➤");
        tg.setTextColor(0xFFFF3B3B);
        tg.setTextSize(30);
        LinearLayout.LayoutParams tgLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        tgLp.rightMargin = dp(16);
        comm.addView(tg, tgLp);

        LinearLayout commCol = new LinearLayout(this);
        commCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        comm.addView(commCol, colLp);

        TextView commTitle = new TextView(this);
        commTitle.setText("Join Community Server");
        commTitle.setTextColor(TEXT_PRIMARY);
        commTitle.setTextSize(15);
        commTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        commCol.addView(commTitle);

        TextView commSub = new TextView(this);
        commSub.setText("To ask, give feedback, give solution, get updates, and more");
        commSub.setTextColor(0xDDE5F5FF);
        commSub.setTextSize(11);
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        csLp.topMargin = dp(4);
        commCol.addView(commSub, csLp);

        comm.setOnClickListener(v -> startActivity(new Intent(
            Intent.ACTION_VIEW, Uri.parse("https://t.me/VantaDisini"))));

        // logout
        LinearLayout logout = new LinearLayout(this);
        logout.setOrientation(LinearLayout.HORIZONTAL);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(dp(28), 0, dp(28), 0);
        GradientDrawable loBg = new GradientDrawable();
        loBg.setColor(TILE_RED);
        loBg.setCornerRadius(dp(48));
        logout.setBackground(loBg);
        LinearLayout.LayoutParams loLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(52));
        loLp.topMargin = dp(36);
        loLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logout, loLp);

        TextView loIcon = new TextView(this);
        loIcon.setText("⏻");
        loIcon.setTextColor(TEXT_PRIMARY);
        loIcon.setTextSize(18);
        LinearLayout.LayoutParams loIconLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        loIconLp.rightMargin = dp(10);
        logout.addView(loIcon, loIconLp);

        TextView loText = new TextView(this);
        loText.setText("LogOut");
        loText.setTextColor(TEXT_PRIMARY);
        loText.setTextSize(14);
        loText.setTypeface(null, android.graphics.Typeface.BOLD);
        logout.addView(loText);

        logout.setOnClickListener(v -> {
            new Preferences(this).putStr("license", "");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setContentView(root);
    }

    private View buildTile(String icon, String label, int color) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(14));
        tile.setBackground(bg);

        TextView ico = new TextView(this);
        ico.setText(icon);
        ico.setTextColor(TEXT_PRIMARY);
        ico.setTextSize(38);
        ico.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams icoLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        icoLp.bottomMargin = dp(14);
        tile.addView(ico, icoLp);

        TextView txt = new TextView(this);
        txt.setText(label);
        txt.setTextColor(TEXT_PRIMARY);
        txt.setTextSize(14);
        txt.setLetterSpacing(0.08f);
        txt.setTypeface(null, android.graphics.Typeface.BOLD);
        txt.setGravity(Gravity.CENTER);
        tile.addView(txt);

        return tile;
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);
        if (r == REQ_OVERLAY && Settings.canDrawOverlays(this)) launchOverlay();
    }

    private void launchOverlay() {
        Intent i = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }
}
