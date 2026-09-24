package com.vanta.codm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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

    private static final int TILE_BLUE       = 0xFF0A6CFF;
    private static final int TILE_RED        = 0xFFE51427;
    private static final int TILE_TEAL       = 0xFF2DC9B9;
    private static final int TILE_PERIWINKLE = 0xFF7A80E0;
    private static final int TILE_DIM        = 0xFF232838;
    private static final int TEXT_PRIMARY    = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY  = 0xFFB0B5BF;
    private static final int AMBER           = 0xFFF5A623;

    // CODM Garena / Global / VN packages. Also try the common MultiSpace /
    // Parallel Space containers when the game is only installed inside one.
    private static final String[] GAME_PKGS = {
        "com.garena.game.codm",
        "com.activision.callofduty.shooter",
    };

    private static final String[] CONTAINER_PKGS = {
        "com.lbe.parallel.intl",       // Parallel Space International
        "com.lbe.parallel",            // Parallel Space
        "com.excean.multiple",         // Multiple Accounts
        "com.excean.gspace",           // GSpace
        "com.clone.android.dual.space",// Dual Space
        "com.polestar.domultiple",     // Do Multiple
        "com.dualspace.multiapp",      // DualSpace
        "com.multi.parallel",          // generic
    };

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

        // two tiles: START / STOP
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

        // Run The Game — resolves through containers
        View run = buildTile("▶", "Run The Game", TILE_TEAL);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(88));
        runLp.topMargin = dp(20);
        root.addView(run, runLp);
        run.setOnClickListener(v -> launchGame());

        // Open Container — direct route into MultiSpace
        View cont = buildTile("⧉", "Open Container", TILE_DIM);
        LinearLayout.LayoutParams contLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
        contLp.topMargin = dp(12);
        root.addView(cont, contLp);
        cont.setOnClickListener(v -> launchContainer());

        // community
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
        loText.setTypeface(null, Typeface.BOLD);
        logout.addView(loText);

        logout.setOnClickListener(v -> {
            new Preferences(this).putStr("license", "");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setContentView(root);
    }

    // ---------- launching logic ----------

    private void launchGame() {
        for (String pkg : GAME_PKGS) {
            Intent i = resolveLauncher(pkg);
            if (i != null) {
                Log.i(TAG, "launching " + pkg);
                try {
                    startActivity(i);
                } catch (Throwable t) {
                    Log.e(TAG, "launch failed", t);
                    Toast.makeText(this, "launch failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        Toast.makeText(this,
            "CODM not launchable from base. Tap Open Container.",
            Toast.LENGTH_LONG).show();
    }

    private Intent resolveLauncher(String pkg) {
        // first try: standard launcher intent
        Intent base = getPackageManager().getLaunchIntentForPackage(pkg);
        if (base != null) return base;

        // fallback: query all MAIN/LAUNCHER activities for the package
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
        if (matches.size() == 1) return matches.get(0);

        // multiple launchers (base + container clones) -> let the user pick
        Intent first = matches.remove(0);
        Intent chooser = Intent.createChooser(first, "Launch CODM — pick instance");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
            matches.toArray(new Intent[0]));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooser;
    }

    private void launchContainer() {
        for (String pkg : CONTAINER_PKGS) {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i != null) {
                Log.i(TAG, "opening container " + pkg);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            }
        }
        Toast.makeText(this,
            "no known container found. Open your MultiSpace app manually.",
            Toast.LENGTH_LONG).show();
    }

    // ---------- tiles ----------

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
