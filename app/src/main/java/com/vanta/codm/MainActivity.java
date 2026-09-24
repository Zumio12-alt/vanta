package com.vanta.codm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "VANTA-MAIN";
    private static final int REQ_OVERLAY = 0x4d4b;
    private static final String PREFS = "vanta_launch";
    private static final String KEY_MODE = "launch_mode";

    private static final int TILE_BLUE       = 0xFF0A6CFF;
    private static final int TILE_RED        = 0xFFE51427;
    private static final int TILE_TEAL       = 0xFF2DC9B9;
    private static final int TILE_PERIWINKLE = 0xFF7A80E0;
    private static final int TILE_DIM        = 0xFF232838;
    private static final int TEXT_PRIMARY    = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY  = 0xFFB0B5BF;
    private static final int AMBER           = 0xFFF5A623;

    private static final String[] CONTAINER_PKGS = {
        "com.amy.virtual",              // MultiSpace (this is the one in your log)
        "com.lbe.parallel.intl",
        "com.lbe.parallel",
        "com.excean.multiple",
        "com.excean.gspace",
        "com.clone.android.dual.space",
        "com.polestar.domultiple",
        "com.dualspace.multiapp",
        "com.x8.sandbox",
        "io.f1vm.f1vm",
        "com.waxmoon.ma.gp",
    };

    private static final int MODE_ASK = 0;
    private static final int MODE_BASE = 1;
    private static final int MODE_CONTAINER = 2;
    private static final int MODE_MANUAL = 3;

    private TextView runGameLabel;

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

        // START / STOP overlay
        LinearLayout rowA = new LinearLayout(this);
        rowA.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowALp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(160));
        rowALp.topMargin = dp(28);
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
                Toast.makeText(this, "overlay running — now open CODM", Toast.LENGTH_SHORT).show();
            }
        });

        View stop = buildTile("❚❚", "STOP OVERLAY", TILE_RED);
        LinearLayout.LayoutParams tLp2 = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        tLp2.leftMargin = dp(10);
        rowA.addView(stop, tLp2);
        stop.setOnClickListener(v ->
            stopService(new Intent(this, OverlayService.class)));

        // Run The Game — adaptive. Changes label based on saved mode.
        View run = buildTile("▶", labelForMode(), TILE_TEAL);
        this.runGameLabel = (TextView) ((LinearLayout) run).getChildAt(1);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(88));
        runLp.topMargin = dp(20);
        root.addView(run, runLp);
        run.setOnClickListener(v -> launchGame());

        // Change launch mode
        View change = buildTile("⚙", "How do you launch CODM?", TILE_DIM);
        LinearLayout.LayoutParams changeLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        changeLp.topMargin = dp(12);
        root.addView(change, changeLp);
        change.setOnClickListener(v -> showModeChooser());

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
        commTitle.setTypeface(null, Typeface.BOLD);
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
        loLp.topMargin = dp(28);
        loLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logout, loLp);

        TextView loText = new TextView(this);
        loText.setText("LogOut");
        loText.setTextColor(TEXT_PRIMARY);
        loText.setTextSize(14);
        loText.setTypeface(null, Typeface.BOLD);
        logout.addView(loText);

        logout.setOnClickListener(v -> {
            new Preferences(this).putStr("license", "");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setContentView(root);
    }

    // ---------------- launch logic ----------------

    private int getMode() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_MODE, MODE_ASK);
    }

    private void setMode(int m) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_MODE, m).apply();
        if (runGameLabel != null) runGameLabel.setText(labelForMode());
    }

    private String labelForMode() {
        switch (getMode()) {
            case MODE_BASE:      return "Run The Game";
            case MODE_CONTAINER: return "Open Clone App";
            case MODE_MANUAL:    return "I'll Open It Myself";
            default:             return "Set Launch Mode";
        }
    }

    private void showModeChooser() {
        AlertDialog d = new AlertDialog.Builder(this)
            .setTitle("How do you launch CODM?")
            .setMessage(
                "CODM aborts if launched directly when its data lives in a clone app.\n\n"
                + "Pick the option that matches how you normally open the game.")
            .setPositiveButton("Base install", (x, y) -> setMode(MODE_BASE))
            .setNegativeButton("Clone app", (x, y) -> setMode(MODE_CONTAINER))
            .setNeutralButton("I launch it myself", (x, y) -> setMode(MODE_MANUAL))
            .create();
        d.show();
    }

    private void launchGame() {
        int mode = getMode();
        if (mode == MODE_ASK) {
            showModeChooser();
            return;
        }
        switch (mode) {
            case MODE_BASE:
                launchBase();
                break;
            case MODE_CONTAINER:
                launchContainer();
                break;
            case MODE_MANUAL:
                Toast.makeText(this,
                    "overlay is running — open CODM however you normally do",
                    Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void launchBase() {
        String[] pkgs = { "com.garena.game.codm", "com.activision.callofduty.shooter" };
        for (String pkg : pkgs) {
            Intent i = resolveLauncher(pkg);
            if (i != null) {
                Log.i(TAG, "base launch " + pkg);
                try {
                    startActivity(i);
                } catch (Throwable t) {
                    Toast.makeText(this, "launch failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        Toast.makeText(this,
            "base install not launchable — switch mode to Clone app",
            Toast.LENGTH_LONG).show();
    }

    private void launchContainer() {
        for (String pkg : CONTAINER_PKGS) {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i != null) {
                Log.i(TAG, "opening container " + pkg);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                Toast.makeText(this, "open CODM from inside " + pkg,
                    Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(this,
            "no clone app found — switch mode to Base install or Manual",
            Toast.LENGTH_LONG).show();
    }

    private Intent resolveLauncher(String pkg) {
        Intent base = getPackageManager().getLaunchIntentForPackage(pkg);
        if (base != null) return base;
        Intent q = new Intent(Intent.ACTION_MAIN);
        q.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> all = getPackageManager().queryIntentActivities(q, 0);
        List<Intent> matches = new ArrayList<>();
        for (ResolveInfo ri : all) {
            if (ri.activityInfo == null) continue;
            if (!ri.activityInfo.packageName.equals(pkg)) continue;
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setComponent(new ComponentName(
                ri.activityInfo.packageName, ri.activityInfo.name));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            matches.add(i);
        }
        if (matches.isEmpty()) return null;
        return matches.get(0);
    }

    // ---------------- tiles ----------------

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
        ico.setTextSize(34);
        ico.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams icoLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        icoLp.bottomMargin = dp(10);
        tile.addView(ico, icoLp);

        TextView txt = new TextView(this);
        txt.setText(label);
        txt.setTextColor(TEXT_PRIMARY);
        txt.setTextSize(14);
        txt.setLetterSpacing(0.08f);
        txt.setTypeface(null, Typeface.BOLD);
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
