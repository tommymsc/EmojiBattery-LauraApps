package com.lauraapps.emojibattery;
public final class BatteryMath {
  private BatteryMath(){}
  public static int percent(int level,int scale) {
    if(level<0 || scale<=0) return -1;
    return Math.max(0,Math.min(100,Math.round(level*100f/scale)));
  }
  public static int mood(int value,boolean charging) {
    if(charging) return 3;
    if(value<0) return 1;
    if(value<=20) return 0;
    return value>=90?2:1;
  }
}
