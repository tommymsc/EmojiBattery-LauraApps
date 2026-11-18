package com.lauraapps.emojibattery;
import android.content.Context;
public final class ThemeCatalog {
  private ThemeCatalog(){}
  // Original character designs for LauraApps; all shapes are drawn by BatteryArt.
  public static final int[] BASE={0xFFFFE7F2,0xFFF1E5FF,0xFFE3FFED,0xFFFFE8D9,0xFFE2F4FF,0xFFF0E9FF};
  public static final int[] ACCENT={0xFFFF639F,0xFF9658F6,0xFF20BE91,0xFFFF9948,0xFF389BEF,0xFF9875EE};
  public static final int[] INK={0xFF613455,0xFF4F336F,0xFF295849,0xFF653B28,0xFF234B74,0xFF45346B};
  public static final int[] BODY={0xFFFFE19B,0xFFC99BFF,0xFF97E974,0xFFFFC98D,0xFF66B9F4,0xFF8E73E7};
  public static final int[] SECONDARY={0xFFFF85B0,0xFFFFA7E9,0xFF6FCE5F,0xFFBB7D4D,0xFFFFFFFF,0xFF5CE0EE};
  public static final int[][] CHARACTERS={{0,1,2,3,4,5}};
  public static final int COUNT=6;
  public static int clamp(int v){return Math.max(0,Math.min(COUNT-1,v));}
  public static int load(Context c){return clamp(c.getSharedPreferences("style",0).getInt("theme",0));}
  public static void save(Context c,int v){c.getSharedPreferences("style",0).edit().putInt("theme",clamp(v)).apply();}
}