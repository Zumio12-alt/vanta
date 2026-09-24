package com.vanta.codm.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class PanelView extends LinearLayout {

    public enum Tab { VISUAL, AIM, MISC, SETTINGS }

    // ---- colors (inline, no R.color) ----
    private static final int BG_PANEL       = 0xF0101319;
    private static final int BG_HEADER      = 0xFF161A22;
    private static final int BG_ROW         = 0xB31B2029;
    private static final int STROKE_PANEL   = 0x22FFFFFF;
    private static final int TEXT_PRIMARY   = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B5BF;
    private static final int TEXT_DIM       = 0xFF6E7380;
    private static final int ACCENT_GREEN   = 0xFF19C37D;
    private static final int TOGGLE_OFF     = 0xFF3A3F4A;
    private static final int RAIL_BG        = 0xF00E1117;
    private static final int RAIL_ACTIVE    = 0xFF19C37D;
    private static final int RAIL_IDLE      = 0xFF8A8F99;

    // ---- views ----
    private final LinearLayout rail;
    private final LinearLayout leftList;
    private final LinearLayout centerList;
    private final LinearLayout keyboard;
    private final LinearLayout nameList;
    private final TextView tabTitle;
    private final TextView tabSubtitle;
    private final TextView tabIconView;
    private final EditText search;

    // ---- state ----
    private Tab active = Tab.VISUAL;
    private final List<LinearLayout> railRows = new ArrayList<>();
    private final List<String> allNames = new ArrayList<>();

    private final int headerHeightPx;

    public PanelView(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        setBackground(rounded(BG_PANEL, 6, 1, STROKE_PANEL));
        headerHeightPx = dp(72);

        allNames.add("[BOT] MIKE HARPER");
        allNames.add("[BOT] ALEX MASON");
        allNames.add("[BOT] SPECIAL OPS 1");
        allNames.add("[BOT] SPECIAL OPS 2");
        allNames.add("[BOT] SPECIAL OPS 3");
        allNames.add("[BOT] SPECIAL OPS 4");
        allNames.add("[BOT] RORK");
        allNames.add("[BOT] DAVID MASON");

        // =============== HEADER (drag handle) ===============
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(BG_HEADER);
        header.setPadding(dp(14), 0, dp(10), 0);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, dp(48)));

        TextView brand = new TextView(ctx);
        brand.setText("VANTA");
        brand.setTextColor(TEXT_PRIMARY);
        brand.setTextSize(14);
        brand.setLetterSpacing(0.12f);
        brand.setTypeface(null, Typeface.BOLD);
        header.addView(brand);

        View sep = new View(ctx);
        sep.setBackgroundColor(STROKE_PANEL);
        LayoutParams sepLp = new LayoutParams(dp(1), dp(22));
        sepLp.leftMargin = dp(12);
        sepLp.rightMargin = dp(12);
        header.addView(sep, sepLp);

        tabIconView = new TextView(ctx);
        tabIconView.setText("◉");
        tabIconView.setTextColor(RAIL_ACTIVE);
        tabIconView.setTextSize(16);
        header.addView(tabIconView);

        tabTitle = new TextView(ctx);
        tabTitle.setText("VISUAL");
        tabTitle.setTextColor(TEXT_PRIMARY);
        tabTitle.setTextSize(13);
        tabTitle.setTypeface(null, Typeface.BOLD);
        tabTitle.setLetterSpacing(0.12f);
        LayoutParams ttLp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        ttLp.leftMargin = dp(8);
        header.addView(tabTitle, ttLp);

        TextView close = new TextView(ctx);
        close.setText("CLOSE");
        close.setTextColor(TEXT_PRIMARY);
        close.setTextSize(12);
        close.setTypeface(null, Typeface.BOLD);
        close.setGravity(Gravity.CENTER);
        close.setBackground(rounded(BG_ROW, 6, 0, 0));
        close.setPadding(dp(16), 0, dp(16), 0);
        header.addView(close, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(32)));
        close.setOnClickListener(v -> setVisibility(GONE));

        // drag from header
        header.setOnTouchListener(new DragListener());

        // =============== SUBTITLE ===============
        tabSubtitle = new TextView(ctx);
        tabSubtitle.setText("SEE ENEMIES THROUGH WALLS");
        tabSubtitle.setTextColor(TEXT_SECONDARY);
        tabSubtitle.setTextSize(10);
        tabSubtitle.setLetterSpacing(0.15f);
        tabSubtitle.setBackgroundColor(BG_HEADER);
        tabSubtitle.setPadding(dp(14), 0, dp(14), dp(8));
        addView(tabSubtitle, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // =============== BODY ===============
        LinearLayout body = new LinearLayout(ctx);
        body.setOrientation(HORIZONTAL);
        body.setBackgroundColor(BG_PANEL);
        addView(body, new LayoutParams(LayoutParams.MATCH_PARENT, dp(380)));

        // -- rail --
        rail = new LinearLayout(ctx);
        rail.setOrientation(VERTICAL);
        rail.setBackgroundColor(RAIL_BG);
        rail.setPadding(0, dp(10), 0, 0);
        body.addView(rail, new LayoutParams(dp(56), LayoutParams.MATCH_PARENT));

        // -- left list --
        ScrollView leftScroll = new ScrollView(ctx);
        leftScroll.setBackgroundColor(BG_PANEL);
        leftScroll.setPadding(dp(10), dp(6), dp(10), dp(6));
        leftList = new LinearLayout(ctx);
        leftList.setOrientation(VERTICAL);
        leftScroll.addView(leftList);
        body.addView(leftScroll, new LayoutParams(dp(180), LayoutParams.MATCH_PARENT));

        View d1 = new View(ctx);
        d1.setBackgroundColor(STROKE_PANEL);
        body.addView(d1, new LayoutParams(dp(1), LayoutParams.MATCH_PARENT));

        // -- center --
        ScrollView centerScroll = new ScrollView(ctx);
        centerScroll.setBackgroundColor(BG_PANEL);
        centerScroll.setPadding(dp(12), dp(6), dp(12), dp(6));
        LinearLayout centerCol = new LinearLayout(ctx);
        centerCol.setOrientation(VERTICAL);

        TextView centerTitle = new TextView(ctx);
        centerTitle.setText("AIMBOT MENU");
        centerTitle.setTextColor(TEXT_PRIMARY);
        centerTitle.setTextSize(15);
        centerTitle.setTypeface(null, Typeface.BOLD);
        centerTitle.setGravity(Gravity.CENTER);
        centerTitle.setLetterSpacing(0.15f);
        centerTitle.setPadding(0, dp(10), 0, dp(10));
        centerCol.addView(centerTitle);

        centerList = new LinearLayout(ctx);
        centerList.setOrientation(VERTICAL);
        centerCol.addView(centerList);
        centerScroll.addView(centerCol);
        body.addView(centerScroll, new LayoutParams(dp(230), LayoutParams.MATCH_PARENT));

        View d2 = new View(ctx);
        d2.setBackgroundColor(STROKE_PANEL);
        body.addView(d2, new LayoutParams(dp(1), LayoutParams.MATCH_PARENT));

        // -- right pane --
        LinearLayout right = new LinearLayout(ctx);
        right.setOrientation(VERTICAL);
        right.setBackgroundColor(BG_PANEL);
        right.setPadding(dp(8), dp(8), dp(8), dp(8));
        body.addView(right, new LayoutParams(dp(250), LayoutParams.MATCH_PARENT));

        search = new EditText(ctx);
        search.setHint("SEARCH");
        search.setHintTextColor(TEXT_DIM);
        search.setTextColor(TEXT_PRIMARY);
        search.setTextSize(12);
        search.setSingleLine(true);
        search.setBackground(rounded(BG_ROW, 6, 0, 0));
        search.setPadding(dp(12), 0, dp(12), 0);
        right.addView(search, new LayoutParams(LayoutParams.MATCH_PARENT, dp(40)));

        keyboard = new LinearLayout(ctx);
        keyboard.setOrientation(VERTICAL);
        LayoutParams kbLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        kbLp.topMargin = dp(6);
        right.addView(keyboard, kbLp);

        TextView srLabel = new TextView(ctx);
        srLabel.setText("SEARCH");
        srLabel.setTextColor(TEXT_SECONDARY);
        srLabel.setTextSize(11);
        srLabel.setGravity(Gravity.CENTER);
        srLabel.setLetterSpacing(0.15f);
        LayoutParams srLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        srLp.topMargin = dp(10);
        right.addView(srLabel, srLp);

        ScrollView nameScroll = new ScrollView(ctx);
        nameList = new LinearLayout(ctx);
        nameList.setOrientation(VERTICAL);
        nameScroll.addView(nameList);
        LayoutParams nsLp = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        nsLp.topMargin = dp(6);
        right.addView(nameScroll, nsLp);

        // =============== FOOTER ===============
        LinearLayout footer = new LinearLayout(ctx);
        footer.setOrientation(HORIZONTAL);
        footer.setBackgroundColor(BG_HEADER);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(14), 0, dp(14), 0);
        addView(footer, new LayoutParams(LayoutParams.MATCH_PARENT, dp(40)));

        TextView slotCount = new TextView(ctx);
        slotCount.setText("0 / 8");
        slotCount.setTextColor(TEXT_SECONDARY);
        slotCount.setTextSize(12);
        slotCount.setGravity(Gravity.CENTER);
        footer.addView(slotCount, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView footAction = new TextView(ctx);
        footAction.setText("MINIMIZE");
        footAction.setTextColor(TEXT_PRIMARY);
        footAction.setTextSize(12);
        footAction.setTypeface(null, Typeface.BOLD);
        footAction.setLetterSpacing(0.12f);
        footer.addView(footAction);
        footAction.setOnClickListener(v -> setVisibility(GONE));

        buildRail();
        buildKeyboard();
        buildNameList();
        selectTab(Tab.VISUAL);
    }

    // =====================================================
    // DRAG — attached to header only
    // =====================================================
    private final class DragListener implements OnTouchListener {
        private float startRawX, startRawY;
        private int startPX, startPY;
        private boolean dragging;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            Object lpObj = getLayoutParams();
            if (!(lpObj instanceof WindowManager.LayoutParams)) return false;
            WindowManager.LayoutParams wlp = (WindowManager.LayoutParams) lpObj;

            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startRawX = e.getRawX();
                    startRawY = e.getRawY();
                    startPX = wlp.x;
                    startPY = wlp.y;
                    dragging = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = e.getRawX() - startRawX;
                    float dy = e.getRawY() - startRawY;
                    if (!dragging && Math.abs(dx) + Math.abs(dy) > 8) dragging = true;
                    if (dragging) {
                        wlp.x = startPX + (int) dx;
                        wlp.y = startPY + (int) dy;
                        WindowManager wm = (WindowManager)
                            getContext().getSystemService(Context.WINDOW_SERVICE);
                        if (wm != null) wm.updateViewLayout(PanelView.this, wlp);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
            }
            return false;
        }
    }

    // =====================================================
    // RAIL
    // =====================================================
    private void buildRail() {
        String[] glyphs = { "◉", "◈", "◎", "▤", "⚙" };
        Tab[] tabs = { Tab.VISUAL, Tab.VISUAL, Tab.AIM, Tab.MISC, Tab.SETTINGS };

        for (int i = 0; i < glyphs.length; ++i) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(6), dp(8), 0, dp(8));

            View indicator = new View(getContext());
            indicator.setBackgroundColor(RAIL_ACTIVE);
            LayoutParams indLp = new LayoutParams(dp(3), dp(26));
            indLp.rightMargin = dp(10);
            indicator.setVisibility(INVISIBLE);
            row.addView(indicator, indLp);

            TextView icon = new TextView(getContext());
            icon.setText(glyphs[i]);
            icon.setTextColor(RAIL_IDLE);
            icon.setTextSize(20);
            icon.setGravity(Gravity.CENTER);
            row.addView(icon, new LayoutParams(dp(30), dp(30)));

            final Tab t = tabs[i];
            row.setOnClickListener(v -> selectTab(t));
            railRows.add(row);
            rail.addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    // =====================================================
    // KEYBOARD
    // =====================================================
    private void buildKeyboard() {
        String[] rows = { "1234567890", "QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM" };
        for (String r : rows) {
            LinearLayout line = new LinearLayout(getContext());
            line.setOrientation(HORIZONTAL);
            for (char c : r.toCharArray()) {
                final char ch = c;
                TextView k = makeKey(String.valueOf(c), 1f);
                k.setOnClickListener(v -> {
                    String s = search.getText().toString() + ch;
                    search.setText(s);
                    filterNames(s);
                });
                line.addView(k);
            }
            keyboard.addView(line);
        }

        LinearLayout util = new LinearLayout(getContext());
        util.setOrientation(HORIZONTAL);

        TextView sp = makeKey("SPACE", 3f);
        sp.setOnClickListener(v -> {
            String s = search.getText().toString() + " ";
            search.setText(s);
            filterNames(s);
        });
        util.addView(sp);

        TextView bs = makeKey("<<", 1f);
        bs.setOnClickListener(v -> {
            String s = search.getText().toString();
            if (!s.isEmpty()) s = s.substring(0, s.length() - 1);
            search.setText(s);
            filterNames(s);
        });
        util.addView(bs);

        TextView clr = makeKey("CLEAR", 1f);
        clr.setOnClickListener(v -> {
            search.setText("");
            filterNames("");
        });
        util.addView(clr);

        keyboard.addView(util);
    }

    private TextView makeKey(String text, float weight) {
        TextView k = new TextView(getContext());
        k.setText(text);
        k.setTextColor(TEXT_PRIMARY);
        k.setTextSize(12);
        k.setTypeface(null, Typeface.BOLD);
        k.setGravity(Gravity.CENTER);
        k.setBackground(rounded(BG_ROW, 4, 0, 0));
        LayoutParams lp = new LayoutParams(0, dp(34), weight);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        k.setLayoutParams(lp);
        return k;
    }

    // =====================================================
    // NAME LIST
    // =====================================================
    private void buildNameList() {
        for (String n : allNames) addName(n);
    }

    private void addName(String n) {
        TextView t = new TextView(getContext());
        t.setText(n);
        t.setTextColor(TEXT_PRIMARY);
        t.setTextSize(12);
        t.setPadding(dp(8), dp(6), dp(8), dp(6));
        nameList.addView(t);
    }

    private void filterNames(String q) {
        nameList.removeAllViews();
        String u = q.toUpperCase();
        for (String n : allNames) {
            if (u.isEmpty() || n.toUpperCase().contains(u)) addName(n);
        }
    }

    // =====================================================
    // TABS
    // =====================================================
    private void selectTab(Tab t) {
        active = t;
        leftList.removeAllViews();
        centerList.removeAllViews();

        for (int i = 0; i < railRows.size(); ++i) {
            LinearLayout row = railRows.get(i);
            // rail_indicator is child 0
            row.getChildAt(0).setVisibility(i == t.ordinal() ? VISIBLE : INVISIBLE);
            // icon color
            TextView icon = (TextView) row.getChildAt(1);
            icon.setTextColor(i == t.ordinal() ? RAIL_ACTIVE : RAIL_IDLE);
        }

        switch (t) {
            case VISUAL: {
                tabTitle.setText("VISUAL");
                tabSubtitle.setText("ESP · WALLHACK · RADAR");
                tabIconView.setText("◈");
                String[] items = {
                    "PLAYER ESP", "SKELETON", "VISIBLE CHECK", "RADAR",
                    "DISTANCE", "HEALTH", "PLAYER NAME", "TEAM COLOR",
                    "VEHICLE NAME", "VEHICLE HEALTH", "VEHICLE DISTANCE",
                    "WEAPONS", "AMMO", "THROWABLES", "ATTACHMENTS", "ARMOR",
                };
                for (String s : items) addLeftCheck(s);
                addCenterToggle("AIMBOT 360",   false);
                addCenterToggle("BULLET TRACK", false);
                addCenterToggle("VISIBLE CHECK",false);
                addCenterToggle("HIDE FOV",     false);
                addCenterValue("AIM STRENGTH", "OFF");
                addCenterValue("TRIGGER",      "NONE");
                addCenterValue("TARGET",       "DISTANCE");
                addCenterValue("POSITION",     "HEAD");
                addCenterValue("FOV",          "0.000");
                break;
            }
            case AIM: {
                tabTitle.setText("AIM");
                tabSubtitle.setText("AIM ASSIST · TRIGGER");
                tabIconView.setText("◎");
                String[] items = {
                    "AIMBOT", "TRIGGER BOT", "SMOOTH AIM", "HITBOX HEAD",
                    "VISIBLE ONLY", "IGNORE KNOCKED", "IGNORE TEAM",
                    "SNAPLINE", "FOV CIRCLE", "PREDICTION",
                };
                for (String s : items) addLeftCheck(s);
                addCenterToggle("AIMBOT 360",   false);
                addCenterToggle("BULLET TRACK", false);
                addCenterToggle("VISIBLE CHECK",false);
                addCenterValue("AIM STRENGTH", "OFF");
                addCenterValue("TRIGGER",      "NONE");
                addCenterValue("TARGET",       "DISTANCE");
                addCenterValue("POSITION",     "HEAD");
                addCenterValue("FOV",          "0.000");
                break;
            }
            case MISC: {
                tabTitle.setText("MISC");
                tabSubtitle.setText("RECOIL · MOVEMENT · UTILITY");
                tabIconView.setText("▤");
                String[] items = {
                    "NO RECOIL", "NO SPREAD", "FAST FIRERATE", "DISABLE OVERHEAT",
                    "INCREASE RANGE", "UNLI SLIDE", "SPEEDHACK", "FASTSWIM",
                    "NO FLASHBANGS", "WEAPON KINETIC", "DISABLE PARACHUTE",
                    "NO CROUCH", "SKIP TUTORIAL", "STREAM HIDE", "ANTI-BAN",
                };
                for (String s : items) addLeftCheck(s);
                addCenterButton("RECOIL");
                addCenterButton("SPREAD");
                addCenterButton("RELOAD");
                addCenterButton("SHAKE");
                addCenterButton("SCOPE");
                addCenterButton("SWITCH");
                addCenterButton("HITBOX");
                break;
            }
            case SETTINGS: {
                tabTitle.setText("SETTINGS");
                tabSubtitle.setText("PREFERENCES");
                tabIconView.setText("⚙");
                addCenterToggle("STREAM MODE",     false);
                addCenterToggle("HIDE MENU ICON",  false);
                addCenterToggle("ANTI-SCREENSHOT", false);
                addCenterValue("BUILD", "1.0.0");
                addCenterValue("REGION", "GARENA");
                break;
            }
        }
    }

    // =====================================================
    // ROW BUILDERS
    // =====================================================
    private void addLeftCheck(String label) {
        final TextView t = new TextView(getContext());
        t.setText("● " + label);
        t.setTextColor(TEXT_PRIMARY);
        t.setTextSize(12);
        t.setPadding(dp(6), dp(10), dp(6), dp(10));
        final boolean[] on = { false };
        t.setOnClickListener(v -> {
            on[0] = !on[0];
            t.setText((on[0] ? "◉ " : "● ") + label);
            t.setTextColor(on[0] ? ACCENT_GREEN : TEXT_PRIMARY);
        });
        leftList.addView(t);
    }

    private void addCenterToggle(String label, boolean def) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView tv = new TextView(getContext());
        tv.setText(label);
        tv.setTextColor(TEXT_PRIMARY);
        tv.setTextSize(13);
        tv.setLetterSpacing(0.05f);
        row.addView(tv, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        final View pill = new View(getContext());
        final boolean[] on = { def };
        pill.setBackground(pillDrawable(def));
        row.addView(pill, new LayoutParams(dp(40), dp(22)));

        row.setOnClickListener(v -> {
            on[0] = !on[0];
            pill.setBackground(pillDrawable(on[0]));
        });

        centerList.addView(row);
    }

    private void addCenterValue(String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView l = new TextView(getContext());
        l.setText(label);
        l.setTextColor(TEXT_SECONDARY);
        l.setTextSize(12);
        row.addView(l, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView v = new TextView(getContext());
        v.setText(value);
        v.setTextColor(TEXT_PRIMARY);
        v.setTextSize(12);
        v.setTypeface(null, Typeface.BOLD);
        row.addView(v);

        centerList.addView(row);
    }

    private void addCenterButton(String label) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        LinearLayout btn = new LinearLayout(getContext());
        btn.setOrientation(HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(rounded(0xFFE51427, 6, 0, 0));
        LayoutParams btnLp = new LayoutParams(0, dp(40), 1f);
        row.addView(btn, btnLp);

        TextView t = new TextView(getContext());
        t.setText(label);
        t.setTextColor(TEXT_PRIMARY);
        t.setTextSize(12);
        t.setTypeface(null, Typeface.BOLD);
        t.setLetterSpacing(0.12f);
        btn.addView(t);

        TextView pct = new TextView(getContext());
        pct.setText("0%");
        pct.setTextColor(TEXT_PRIMARY);
        pct.setTextSize(12);
        pct.setGravity(Gravity.END);
        LayoutParams pLp = new LayoutParams(dp(48), LayoutParams.WRAP_CONTENT);
        row.addView(pct, pLp);

        centerList.addView(row);
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private GradientDrawable rounded(int color, int radiusDp, int strokeWidthDp, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if (strokeWidthDp > 0) g.setStroke(dp(strokeWidthDp), strokeColor);
        return g;
    }

    private GradientDrawable pillDrawable(boolean on) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(on ? ACCENT_GREEN : TOGGLE_OFF);
        g.setCornerRadius(dp(12));
        return g;
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }
}
