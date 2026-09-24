package com.vanta.codm;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class LoaderActivity extends Activity {

    private static final int TILE_BLUE = 0xFF0A6CFF;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B5BF;
    private static final int TEXT_POSITIVE = 0xFF19C37D;
    private static final int TEXT_NEGATIVE = 0xFFFF3B3B;
    private static final int TEXT_AMBER    = 0xFFF5A623;
    private static final int TEXT_DIM = 0xFF6E7380;

    // label, package, region code
    private static final String[][] TARGETS = {
        { "CODM GARENA", "com.garena.game.codm",              "GARENA" },
        { "CODM GLOBAL", "com.activision.callofduty.shooter", "GLOBAL" },
        { "CODM VN",     "com.vng.codmvn",                    "VIETNAM" },
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(20), dp(48), dp(20), dp(24));

        TextView brand = new TextView(this);
        brand.setText("VANTA");
        brand.setTextColor(TEXT_PRIMARY);
        brand.setTextSize(26);
        brand.setLetterSpacing(0.15f);
        brand.setTypeface(null, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);

        TextView sub = new TextView(this);
        sub.setText("LOADER");
        sub.setTextColor(TEXT_SECONDARY);
        sub.setTextSize(12);
        sub.setLetterSpacing(0.45f);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        listLp.topMargin = dp(28);
        root.addView(list, listLp);

        for (String[] t : TARGETS) {
            list.addView(buildTargetRow(t[0], t[1], t[2]));
        }

        // permission card — shows when MANAGE_EXTERNAL_STORAGE isn't granted
        if (!hasStorageAccess()) {
            LinearLayout perm = new LinearLayout(this);
            perm.setOrientation(LinearLayout.HORIZONTAL);
            perm.setGravity(Gravity.CENTER_VERTICAL);
            perm.setPadding(dp(16), dp(12), dp(16), dp(12));
            GradientDrawable permBg = new GradientDrawable();
            permBg.setColor(0x33F5A623);
            permBg.setCornerRadius(dp(10));
            perm.setBackground(permBg);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            pLp.topMargin = dp(12);
            list.addView(perm, pLp);

            TextView pt = new TextView(this);
            pt.setText("grant 'All files access' so Vanta can see "
                + "games hidden inside MultiSpace");
            pt.setTextColor(TEXT_PRIMARY);
            pt.setTextSize(11);
            pt.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            perm.addView(pt);

            TextView pa = new TextView(this);
            pa.setText("GRANT");
            pa.setTextColor(TEXT_AMBER);
            pa.setTextSize(12);
            pa.setTypeface(null, Typeface.BOLD);
            pa.setPadding(dp(12), 0, 0, 0);
            perm.addView(pa);

            perm.setOnClickListener(v -> requestStorageAccess());
        }

        TextView hint = new TextView(this);
        hint.setText("if a game shows 'hidden by MultiSpace' but you have it "
            + "installed in the container, tap Open Menu anyway.");
        hint.setTextColor(TEXT_DIM);
        hint.setTextSize(10);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(12), dp(8), dp(12), dp(8));
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        hLp.topMargin = dp(8);
        list.addView(hint, hLp);

        TextView welcome = new TextView(this);
        welcome.setText("~ Welcome: VantaExternalCODM ~");
        welcome.setTextColor(TEXT_SECONDARY);
        welcome.setTextSize(13);
        welcome.setGravity(Gravity.CENTER);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(0xB31B2029);
        pillBg.setCornerRadius(dp(48));
        pillBg.setStroke(dp(1), 0x18FFFFFF);
        welcome.setBackground(pillBg);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        wLp.topMargin = dp(16);
        list.addView(welcome, wLp);

        setContentView(root);
    }

    private View buildTargetRow(String name, String pkg, String region) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(0xB31B2029);
        rowBg.setCornerRadius(dp(10));
        row.setBackground(rowBg);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(12);
        row.setLayoutParams(rowLp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(col, colLp);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(TEXT_PRIMARY);
        nameView.setTextSize(16);
        nameView.setTypeface(null, Typeface.BOLD);
        col.addView(nameView);

        TextView status = new TextView(this);
        status.setTextSize(12);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        stLp.topMargin = dp(4);
        col.addView(status, stLp);

        int detected = detectState(pkg);
        switch (detected) {
            case 2:  // visible to base PM
                status.setText("Installed (base)");
                status.setTextColor(TEXT_POSITIVE);
                break;
            case 1:  // hidden by container, data dir present
                status.setText("Hidden by MultiSpace (data present)");
                status.setTextColor(TEXT_AMBER);
                break;
            case 0:  // data dir present but PM blind — probably installed in container
                status.setText("Data folder present (install state unknown)");
                status.setTextColor(TEXT_AMBER);
                break;
            default: // not found anywhere
                status.setText("Not found");
                status.setTextColor(TEXT_NEGATIVE);
                break;
        }

        Button run = new Button(this);
        run.setText("Open Menu");
        run.setTextColor(TEXT_PRIMARY);
        run.setTextSize(13);
        run.setAllCaps(false);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(TILE_BLUE);
        btnBg.setCornerRadius(dp(10));
        run.setBackground(btnBg);
        run.setPadding(dp(22), 0, dp(22), 0);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(44));
        row.addView(run, runLp);

        run.setOnClickListener(v -> {
            // store the region for later use
            new Preferences(this).putStr("region", region);
            startActivity(new Intent(this, MainActivity.class));
        });

        return row;
    }

    /**
     * 2 = visible to base PackageManager
     * 1 = PM can't see it, but data dir exists (hidden by MultiSpace)
     * 0 = PM can't see it, data dir not readable (no all-files access?)
     * -1 = nowhere
     */
    private int detectState(String pkg) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
            return 2;
        } catch (Throwable ignored) { }

        if (hasStorageAccess()) {
            File data = new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + pkg);
            File obb  = new File(Environment.getExternalStorageDirectory(),
                "Android/obb/" + pkg);
            if (data.exists() || obb.exists()) return 1;
            return -1;
        }
        return 0;
    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT < 30) return true;
        try {
            return Environment.isExternalStorageManager();
        } catch (Throwable t) {
            return false;
        }
    }

    private void requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                Intent i = new Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivity(i);
                Toast.makeText(this,
                    "enable the toggle, then come back",
                    Toast.LENGTH_LONG).show();
            } catch (Throwable t) {
                try {
                    startActivity(new Intent(
                        Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                } catch (Throwable t2) {
                    Toast.makeText(this,
                        "open Settings → Apps → Special access → All files access",
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // rebuild UI when coming back from the settings screen so statuses refresh
        onCreate(null);
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }
}
