package com.lauraapps.emojibattery;
import android.content.Context;
public final class ThemeCatalog {
  private ThemeCatalog(){}
  public static final int[] BASE={0xFFFFE6F0,0xFFEEE8FF,0xFFE0F9EC,0xFF29263C};
  public static final int[] ACCENT={0xFFEB568E,0xFF8E70E7,0xFF16AB84,0xFFB6A5FF};
  public static final int[] INK={0xFF503047,0xFF41335C,0xFF264C3F,0xFFFDFBFF};
  public static final String[][] FACES={{"🥺","😊","😍","🥰"},{"😴","😌","🤩","✨"},{"😟","🙂","😎","⚡"},{"🌙","😺","🌟","💜"}};
  public static int clamp(int v){return Math.max(0,Math.min(BASE.length-1,v));}
  public static int load(Context c){return clamp(c.getSharedPreferences("style",0).getInt("theme",0));}
  public static void save(Context c,int v){c.getSharedPreferences("style",0).edit().putInt("theme",clamp(v)).apply();}
}
