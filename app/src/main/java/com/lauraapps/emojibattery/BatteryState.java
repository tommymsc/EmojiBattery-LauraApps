package com.lauraapps.emojibattery;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
public final class BatteryState {
  public final int percent; public final boolean charging;
  private BatteryState(int p,boolean c){percent=p;charging=c;}
  public static BatteryState read(Context c) {
    Intent battery=c.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    if(battery==null)return new BatteryState(-1,false);
    int level=battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
    int scale=battery.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
    int status=battery.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
    return new BatteryState(BatteryMath.percent(level,scale), status==BatteryManager.BATTERY_STATUS_CHARGING || status==BatteryManager.BATTERY_STATUS_FULL);
  }
}
