package com.lauraapps.emojibattery;
import android.content.Context;
public final class ThemeCatalog {
 private ThemeCatalog(){}
 public static final int COUNT=12;
 public static final int[] BASE={
  0xFFFFE7F2,0xFFF1E5FF,0xFFE3FFED,0xFFFFE8D9,0xFFE2F4FF,0xFFF0E9FF,
  0xFFFFECDA,0xFFE1DCFF,0xFFE7FFF4,0xFFFFECF1,0xFFEAF6FF,0xFFE5DCFF};
 public static final int[] ACCENT={
  0xFFFF639F,0xFF9658F6,0xFF20BE91,0xFFFF9948,0xFF389BEF,0xFF9875EE,
  0xFFFFB23A,0xFF8069EC,0xFF5DC49A,0xFFFF729B,0xFF4B89DD,0xFF6253C7};
 public static final int[] INK={
  0xFF613455,0xFF4F336F,0xFF295849,0xFF653B28,0xFF234B74,0xFF45346B,
  0xFF725337,0xFF484072,0xFF36644C,0xFF664054,0xFF284E72,0xFF3E3377};
 public static final int[] BODY={
  0xFFFFE19B,0xFFC99BFF,0xFF97E974,0xFFFFC98D,0xFF66B9F4,0xFF8E73E7,
  0xFFFFD28F,0xFFA3A1FC,0xFFB6E6AF,0xFFFFC5A1,0xFF93C5F8,0xFF9E82E4};
 public static final int[] SECONDARY={
  0xFFFF85B0,0xFFFFA7E9,0xFF6FCE5F,0xFFBB7D4D,0xFFFFFFFF,0xFF5CE0EE,
  0xFFFF9B89,0xFFDEAAEF,0xFF83D5B6,0xFFD8A07A,0xFFFFFFFF,0xFF78BFEF};
 public static int clamp(int v){return Math.max(0,Math.min(COUNT-1,v));}
 public static int load(Context c){return clamp(c.getSharedPreferences("style",0).getInt("theme",0));}
 public static void save(Context c,int v){c.getSharedPreferences("style",0).edit().putInt("theme",clamp(v)).apply();}
}