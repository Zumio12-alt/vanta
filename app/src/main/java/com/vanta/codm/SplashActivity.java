package com.vanta.codm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);

        TextView brand = new TextView(this);
        brand.setText("VANTA");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(52);
        brand.setLetterSpacing(0.35f);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);

        TextView sub = new TextView(this);
        sub.setText("CODM LOADER");
        sub.setTextColor(0xFF888888);
        sub.setTextSize(12);
        sub.setLetterSpacing(0.5f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 0);
        root.addView(sub);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 6);
        int m = (int)(40 * getResources().getDisplayMetrics().density);
        lp.setMargins(m, 60, m, 0);
        root.addView(pb, lp);

        setContentView(root);

        // animate progress bar to 100 over ~1.2s, then route to login
        final int[] p = {0};
        Handler h = new Handler(Looper.getMainLooper());
        Runnable r = new Runnable() {
            @Override public void run() {
                p[0] += 4;
                if (p[0] > 100) p[0] = 100;
                pb.setProgress(p[0]);
                if (p[0] < 100) {
                    h.postDelayed(this, 40);
                } else {
                    h.postDelayed(() -> {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }, 200);
                }
            }
        };
        h.postDelayed(r, 200);
    }
}
