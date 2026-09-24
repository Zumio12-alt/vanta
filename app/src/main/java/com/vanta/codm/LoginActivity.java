package com.vanta.codm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private static final String TAG = "VANTA-LOGIN";
    private static final int ACCENT_RED = 0xFFFF0A28;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B5BF;
    private static final int TEXT_DIM = 0xFF6E7380;

    private LinearLayout root;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Log.i(TAG, "onCreate entered");

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(28), dp(72), dp(28), dp(40));
        setContentView(root);
        Log.i(TAG, "setContentView ok");

        try {
            buildUi();
            Log.i(TAG, "buildUi ok");
        } catch (Throwable t) {
            Log.e(TAG, "buildUi failed", t);
            root.removeAllViews();
            TextView err = new TextView(this);
            err.setText("LOGIN BUILD FAILED:\n\n"
                + t.getClass().getName() + "\n\n"
                + t.getMessage());
            err.setTextColor(0xFFFF3B3B);
            err.setTextSize(12);
            err.setPadding(dp(16), dp(16), dp(16), dp(16));
            root.addView(err);
        }
    }

    private void buildUi() {
        TextView avatar = new TextView(this);
        avatar.setText("V");
        avatar.setTextColor(TEXT_PRIMARY);
        avatar.setTextSize(56);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(0xFF1B2029);
        circle.setStroke(dp(2), 0x22FFFFFF);
        avatar.setBackground(circle);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(128), dp(128));
        root.addView(avatar, avLp);

        TextView hello = new TextView(this);
        hello.setText("Hello, please sign in to your account");
        hello.setTextColor(TEXT_SECONDARY);
        hello.setTextSize(14);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        hLp.topMargin = dp(20);
        root.addView(hello, hLp);

        LinearLayout inputBox = new LinearLayout(this);
        inputBox.setOrientation(LinearLayout.HORIZONTAL);
        inputBox.setGravity(Gravity.CENTER_VERTICAL);
        inputBox.setPadding(dp(16), 0, dp(16), 0);
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setShape(GradientDrawable.RECTANGLE);
        inputBg.setColor(Color.BLACK);
        inputBg.setStroke(dp(2), ACCENT_RED);
        inputBg.setCornerRadius(dp(10));
        inputBox.setBackground(inputBg);
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        boxLp.topMargin = dp(28);
        root.addView(inputBox, boxLp);

        TextView person = new TextView(this);
        person.setText("●");
        person.setTextColor(ACCENT_RED);
        person.setTextSize(20);
        inputBox.addView(person);

        final EditText key = new EditText(this);
        key.setHint("Enter license key");
        key.setHintTextColor(TEXT_DIM);
        key.setTextColor(TEXT_PRIMARY);
        key.setTextSize(14);
        key.setBackgroundColor(Color.TRANSPARENT);
        key.setSingleLine(true);
        key.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams keyLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        keyLp.leftMargin = dp(12);
        inputBox.addView(key, keyLp);

        final Preferences prefs = new Preferences(this);
        String saved = prefs.getStr("license", "");
        if (!saved.isEmpty()) key.setText(saved);

        final TextView status = new TextView(this);
        status.setTextColor(0xFFFF3B3B);
        status.setTextSize(12);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        sLp.topMargin = dp(8);
        root.addView(status, sLp);

        Button login = new Button(this);
        login.setText("LOGIN");
        login.setTextColor(TEXT_PRIMARY);
        login.setTextSize(15);
        login.setAllCaps(false);
        login.setTypeface(null, Typeface.BOLD);
        GradientDrawable pill = new GradientDrawable();
        pill.setShape(GradientDrawable.RECTANGLE);
        pill.setColor(ACCENT_RED);
        pill.setCornerRadius(dp(48));
        login.setBackground(pill);
        LinearLayout.LayoutParams loginLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        loginLp.topMargin = dp(24);
        root.addView(login, loginLp);

        login.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String k = key.getText().toString().trim();
                if (k.length() < 4) {
                    status.setText("key too short");
                    return;
                }
                prefs.putStr("license", k);
                startActivity(new Intent(LoginActivity.this, LoaderActivity.class));
                finish();
            }
        });

        TextView buy = new TextView(this);
        buy.setText("Buy Key ? Contact me on");
        buy.setTextColor(TEXT_SECONDARY);
        buy.setTextSize(13);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        bLp.topMargin = dp(28);
        root.addView(buy, bLp);

        TextView tg = new TextView(this);
        tg.setText("➤");
        tg.setTextColor(ACCENT_RED);
        tg.setTextSize(36);
        tg.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        tLp.topMargin = dp(6);
        root.addView(tg, tLp);
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }
}
