package com.lauraapps.emojibattery;
public final class BatteryMath {
  private BatteryMath(){}
  public static int percent(int level,int scale){
    if(level<0||scale<=0)return -1;
    return Math.max(0,Math.min(100,Math.round(level*100f/scale)));
  }
  public static int mood(int value,boolean charging){
    if(charging)return 4;
    if(value<0)return 2;
    if(value<=20)return 0;
    if(value<=45)return 1;
    if(value>=85)return 3;
    return 2;
  }
}