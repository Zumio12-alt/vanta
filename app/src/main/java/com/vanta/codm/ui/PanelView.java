package com.vanta.codm.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanta.codm.R;

import java.util.ArrayList;
import java.util.List;

public final class PanelView extends FrameLayout {

    public enum Tab { VISUAL, AIM, MISC, SETTINGS }

    private final LinearLayout rail, leftList, centerList, keyboard, nameList;
    private final TextView centerTitle, tabTitle, tabSubtitle;
    private final ImageView tabIcon;
    private final EditText search;
    private Tab active = Tab.VISUAL;

    // drag state
    private boolean inHeader = false;
    private boolean dragging = false;
    private float startRawX, startRawY;
    private int startPX, startPY;
    private final int headerHeightPx;

    public PanelView(Context ctx) {
        super(ctx);
        headerHeightPx = (int)(72 * ctx.getResources().getDisplayMetrics().density);

        LayoutInflater.from(ctx).inflate(R.layout.overlay_menu, this, true);

        rail        = findViewById(R.id.rail);
        leftList    = findViewById(R.id.left_list);
        centerList  = findViewById(R.id.center_list);
        keyboard    = findViewById(R.id.keyboard);
        nameList    = findViewById(R.id.name_list);
        centerTitle = findViewById(R.id.center_title);
        tabTitle    = findViewById(R.id.tab_title);
        tabSubtitle = findViewById(R.id.tab_subtitle);
        tabIcon     = findViewById(R.id.tab_icon);
        search      = findViewById(R.id.search_field);

        ((TextView)findViewById(R.id.brand)).setText(R.string.brand);

        findViewById(R.id.close).setOnClickListener(v -> setVisibility(GONE));
        findViewById(R.id.footer_action).setOnClickListener(v -> setVisibility(GONE));

        buildRail();
        buildKeyboard();
        buildNameList();
        selectTab(Tab.VISUAL);
    }

    // --- drag handling: only from header strip -------------------------------

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                inHeader = ev.getY() < headerHeightPx;
                if (inHeader) {
                    startRawX = ev.getRawX();
                    startRawY = ev.getRawY();
                    Object lp = getLayoutParams();
                    if (lp instanceof WindowManager.LayoutParams) {
                        startPX = ((WindowManager.LayoutParams) lp).x;
                        startPY = ((WindowManager.LayoutParams) lp).y;
                    }
                    dragging = false;
                }
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!inHeader) return false;
                float dx = Math.abs(ev.getRawX() - startRawX);
                float dy = Math.abs(ev.getRawY() - startRawY);
                if (dx > 8 || dy > 8) dragging = true;
                return dragging;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                boolean was = dragging;
                inHeader = false;
                dragging = false;
                return was;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!dragging) return false;
        Object lpObj = getLayoutParams();
        if (!(lpObj instanceof WindowManager.LayoutParams)) return false;
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) lpObj;

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE: {
                lp.x = startPX + (int)(ev.getRawX() - startRawX);
                lp.y = startPY + (int)(ev.getRawY() - startRawY);
                WindowManager wm = (WindowManager)
                    getContext().getSystemService(Context.WINDOW_SERVICE);
                if (wm != null) wm.updateViewLayout(this, lp);
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                dragging = false;
                inHeader = false;
                return true;
            }
        }
        return false;
    }

    // --- tab building (unchanged) --------------------------------------------

    private void buildRail() {
        LayoutInflater inf = LayoutInflater.from(getContext());
        int[] icons = { R.drawable.ic_rail_home, R.drawable.ic_rail_visual,
                        R.drawable.ic_rail_aim,  R.drawable.ic_rail_misc,
                        R.drawable.ic_rail_settings };
        Tab[] tabs = { Tab.VISUAL, Tab.VISUAL, Tab.AIM, Tab.MISC, Tab.SETTINGS };
        for (int i = 0; i < icons.length; ++i) {
            View row = inf.inflate(R.layout.item_rail_icon, rail, false);
            ((ImageView) row.findViewById(R.id.rail_icon)).setImageResource(icons[i]);
            final Tab t = tabs[i];
            row.setOnClickListener(v -> selectTab(t));
            rail.addView(row);
        }
    }

    private void buildKeyboard() {
        String[] rows = { "1234567890", "QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM" };
        LayoutInflater inf = LayoutInflater.from(getContext());
        for (String r : rows) {
            LinearLayout line = new LinearLayout(getContext());
            line.setOrientation(LinearLayout.HORIZONTAL);
            for (char c : r.toCharArray()) {
                TextView k = (TextView) inf.inflate(R.layout.item_qwerty_key, line, false);
                k.setText(String.valueOf(c));
                k.setOnClickListener(v -> {
                    String s = search.getText().toString() + c;
                    search.setText(s);
                    filterNames(s);
                });
                line.addView(k);
            }
            keyboard.addView(line);
        }
        LinearLayout util = new LinearLayout(getContext());
        util.setOrientation(LinearLayout.HORIZONTAL);
        String[] utilKeys = { "SPACE", "<<", "CLEAR" };
        for (String u : utilKeys) {
            TextView k = (TextView) inf.inflate(R.layout.item_qwerty_key, util, false);
            k.setText(u);
            k.setOnClickListener(v -> {
                String s = search.getText().toString();
                if (u.equals("SPACE")) s += " ";
                else if (u.equals("<<")) s = s.isEmpty() ? s : s.substring(0, s.length() - 1);
                else s = "";
                search.setText(s);
                filterNames(s);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, u.equals("SPACE") ? 3f : 1f);
            lp.setMargins(4, 4, 4, 4);
            k.setLayoutParams(lp);
            util.addView(k);
        }
        keyboard.addView(util);
    }

    private final List<String> allNames = new ArrayList<>();
    {
        allNames.add("[BOT] MIKE HARPER");
        allNames.add("[BOT] ALEX MASON");
        allNames.add("[BOT] SPECIAL OPS 1");
        allNames.add("[BOT] SPECIAL OPS 2");
        allNames.add("[BOT] SPECIAL OPS 3");
        allNames.add("[BOT] SPECIAL OPS 4");
        allNames.add("[BOT] RORK");
        allNames.add("[BOT] DAVID MASON");
    }

    private void buildNameList() {
        for (String n : allNames) {
            TextView t = new TextView(getContext());
            t.setText(n);
            t.setTextColor(getColor(R.color.text_primary));
            t.setTextSize(12);
            t.setPadding(8, 6, 8, 6);
            nameList.addView(t);
        }
    }

    private void filterNames(String q) {
        nameList.removeAllViews();
        String u = q.toUpperCase();
        for (String n : allNames) {
            if (u.isEmpty() || n.toUpperCase().contains(u)) {
                TextView t = new TextView(getContext());
                t.setText(n);
                t.setTextColor(getColor(R.color.text_primary));
                t.setTextSize(12);
                t.setPadding(8, 6, 8, 6);
                nameList.addView(t);
            }
        }
    }

    private int getColor(int id) { return getResources().getColor(id, null); }

    private void selectTab(Tab t) {
        active = t;
        leftList.removeAllViews();
        centerList.removeAllViews();

        for (int i = 0; i < rail.getChildCount(); ++i) {
            View row = rail.getChildAt(i);
            View ind = row.findViewById(R.id.rail_indicator);
            ind.setVisibility(i == t.ordinal() ? VISIBLE : INVISIBLE);
        }

        LayoutInflater inf = LayoutInflater.from(getContext());

        switch (t) {
            case VISUAL: {
                tabTitle.setText("VISUAL");
                tabSubtitle.setText("ESP · WALLHACK · RADAR");
                tabIcon.setImageResource(R.drawable.ic_rail_visual);
                String[] items = {
                    "PLAYER ESP", "SKELETON", "VISIBLE CHECK", "RADAR",
                    "DISTANCE", "HEALTH", "PLAYER NAME", "TEAM COLOR",
                    "VEHICLE NAME", "VEHICLE HEALTH", "VEHICLE DISTANCE",
                    "WEAPONS", "AMMO", "THROWABLES", "ATTACHMENTS", "ARMOR",
                };
                for (String s : items) addLeftCheck(inf, s);
                centerTitle.setText("AIMBOT MENU");
                addCenterToggle(inf, "AIMBOT 360",  false);
                addCenterToggle(inf, "BULLET TRACK", false);
                addCenterToggle(inf, "VISIBLE CHECK", false);
                addCenterToggle(inf, "HIDE FOV",     false);
                addCenterValue(inf, "AIM STRENGTH", "OFF");
                addCenterValue(inf, "TRIGGER",      "NONE");
                addCenterValue(inf, "TARGET",       "DISTANCE");
                addCenterValue(inf, "POSITION",     "HEAD");
                addCenterValue(inf, "FOV",          "0.000");
                break;
            }
            case AIM: {
                tabTitle.setText("AIM");
                tabSubtitle.setText("AIM ASSIST · TRIGGER");
                tabIcon.setImageResource(R.drawable.ic_rail_aim);
                String[] items = {
                    "AIMBOT", "TRIGGER BOT", "SMOOTH AIM", "HITBOX HEAD",
                    "VISIBLE ONLY", "IGNORE KNOCKED", "IGNORE TEAM",
                    "SNAPLINE", "FOV CIRCLE", "PREDICTION",
                };
                for (String s : items) addLeftCheck(inf, s);
                centerTitle.setText("AIM TOUCH");
                addCenterToggle(inf, "AIMBOT 360",   false);
                addCenterToggle(inf, "BULLET TRACK", false);
                addCenterToggle(inf, "VISIBLE CHECK", false);
                addCenterValue(inf, "AIM STRENGTH", "OFF");
                addCenterValue(inf, "TRIGGER",      "NONE");
                addCenterValue(inf, "TARGET",       "DISTANCE");
                addCenterValue(inf, "POSITION",     "HEAD");
                addCenterValue(inf, "FOV",          "0.000");
                break;
            }
            case MISC: {
                tabTitle.setText("MISC");
                tabSubtitle.setText("RECOIL · MOVEMENT · UTILITY");
                tabIcon.setImageResource(R.drawable.ic_rail_misc);
                String[] items = {
                    "NO RECOIL", "NO SPREAD", "FAST FIRERATE", "DISABLE OVERHEAT",
                    "INCREASE RANGE", "UNLI SLIDE", "SPEEDHACK", "FASTSWIM",
                    "NO FLASHBANGS", "WEAPON KINETIC", "DISABLE PARACHUTE",
                    "NO CROUCH", "SKIP TUTORIAL", "STREAM HIDE", "ANTI-BAN",
                };
                for (String s : items) addLeftCheck(inf, s);
                centerTitle.setText("QUICK HACKS");
                addCenterButton(inf, "RECOIL", "0%");
                addCenterButton(inf, "SPREAD", "0%");
                addCenterButton(inf, "RELOAD", "0%");
                addCenterButton(inf, "SHAKE",  "0%");
                addCenterButton(inf, "SCOPE",  "0%");
                addCenterButton(inf, "SWITCH", "0%");
                addCenterButton(inf, "HITBOX", "0%");
                break;
            }
            case SETTINGS: {
                tabTitle.setText("SETTINGS");
                tabSubtitle.setText("PREFERENCES");
                tabIcon.setImageResource(R.drawable.ic_rail_settings);
                centerTitle.setText("PREFERENCES");
                addCenterToggle(inf, "STREAM MODE",     false);
                addCenterToggle(inf, "HIDE MENU ICON",  false);
                addCenterToggle(inf, "ANTI-SCREENSHOT", false);
                addCenterValue(inf, "BUILD", "1.0.0");
                addCenterValue(inf, "REGION", "GARENA");
                break;
            }
        }
    }

    private void addLeftCheck(LayoutInflater inf, String label) {
        TextView t = new TextView(getContext());
        t.setText("● " + label);
        t.setTextColor(getColor(R.color.text_primary));
        t.setTextSize(12);
        t.setPadding(6, 10, 6, 10);
        final boolean[] on = {false};
        t.setOnClickListener(v -> {
            on[0] = !on[0];
            t.setText((on[0] ? "◉ " : "● ") + label);
            t.setTextColor(getColor(on[0] ? R.color.accent_green : R.color.text_primary));
        });
        leftList.addView(t);
    }

    private void addCenterToggle(LayoutInflater inf, String label, boolean def) {
        View row = inf.inflate(R.layout.item_row_toggle, centerList, false);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        View pill = row.findViewById(R.id.pill);
        final boolean[] on = {def};
        pill.setBackgroundResource(def ? R.drawable.switch_pill_on : R.drawable.switch_pill_off);
        row.setOnClickListener(v -> {
            on[0] = !on[0];
            pill.setBackgroundResource(on[0] ? R.drawable.switch_pill_on : R.drawable.switch_pill_off);
        });
        centerList.addView(row);
    }

    private void addCenterValue(LayoutInflater inf, String label, String value) {
        View row = inf.inflate(R.layout.item_row_value, centerList, false);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        centerList.addView(row);
    }

    private void addCenterButton(LayoutInflater inf, String label, String pct) {
        View row = inf.inflate(R.layout.item_row_button, centerList, false);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        ((TextView) row.findViewById(R.id.pct)).setText(pct);
        centerList.addView(row);
    }
}
