package com.vanta.codm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoaderActivity extends Activity {

    private static final int TILE_BLUE = 0xFF0A6CFF;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B5BF;
    private static final int TEXT_POSITIVE = 0xFF19C37D;
    private static final int TEXT_NEGATIVE = 0xFFFF3B3B;
    private static final int TEXT_DIM = 0xFF6E7380;

    private static final String[][] TARGETS = {
        { "CODM GARENA", "com.garena.game.codm" },
        { "CODM GLOBAL", "com.activision.callofduty.shooter" },
        { "CODM VN",     "com.garena.game.codm" },
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
        listLp.topMargin = dp(36);
        root.addView(list, listLp);

        for (String[] t : TARGETS) {
            list.addView(buildTargetRow(t[0], t[1]));
        }

        TextView hint = new TextView(this);
        hint.setText("if your game is hidden inside MultiSpace, status will read "
            + "'Not visible' — that is normal. Tap Open Menu anyway.");
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
        wLp.topMargin = dp(20);
        list.addView(welcome, wLp);

        setContentView(root);
    }

    private View buildTargetRow(String name, String pkg) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(0xB31B2029);
        rowBg.setCornerRadius(dp(10));
        row.setBackground(rowBg);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(16);
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

        // status is informational only — button always works
        boolean visible = isVisible(pkg);
        if (visible) {
            status.setText("Visible to base");
            status.setTextColor(TEXT_POSITIVE);
        } else {
            status.setText("Not visible (hidden by MultiSpace)");
            status.setTextColor(TEXT_NEGATIVE);
        }

        run.setOnClickListener(v ->
            startActivity(new Intent(this, MainActivity.class)));

        return row;
    }

    private boolean isVisible(String pkg) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }
}
