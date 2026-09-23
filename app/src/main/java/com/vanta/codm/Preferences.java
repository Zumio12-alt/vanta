package com.vanta.codm;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
    private static final String SP = "vanta_prefs";
    private final SharedPreferences sp;

    public Preferences(Context ctx) {
        sp = ctx.getSharedPreferences(SP, Context.MODE_PRIVATE);
    }

    public boolean getBool(String k, boolean d) { return sp.getBoolean(k, d); }
    public int     getInt (String k, int d)     { return sp.getInt(k, d); }
    public float   getFloat(String k, float d)  { return sp.getFloat(k, d); }
    public String  getStr (String k, String d)  { return sp.getString(k, d); }

    public void putBool (String k, boolean v) { sp.edit().putBoolean(k, v).apply(); }
    public void putInt  (String k, int v)     { sp.edit().putInt(k, v).apply(); }
    public void putFloat(String k, float v)   { sp.edit().putFloat(k, v).apply(); }
    public void putStr  (String k, String v)  { sp.edit().putString(k, v).apply(); }
}
