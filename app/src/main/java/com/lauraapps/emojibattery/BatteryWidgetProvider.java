package com.lauraapps.emojibattery;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class BatteryWidgetProvider extends AppWidgetProvider {
  private static final ExecutorService worker=Executors.newSingleThreadExecutor();
  public static void refreshAsync(Context c){Context app=c.getApplicationContext();worker.execute(()->refresh(app));}
  @Override public void onUpdate(Context c,AppWidgetManager am,int[] ids){for(int id:ids)updateOne(c,am,id);}
  @Override public void onDeleted(Context c,int[] ids){
    SharedPreferences.Editor e=c.getSharedPreferences("widget_themes",0).edit();
    for(int id:ids)e.remove("theme_"+id);
    e.apply();
    super.onDeleted(c,ids);
  }
  @Override public void onReceive(Context c,Intent i){super.onReceive(c,i);String a=i.getAction();if(Intent.ACTION_POWER_CONNECTED.equals(a)||Intent.ACTION_POWER_DISCONNECTED.equals(a)||Intent.ACTION_BATTERY_LOW.equals(a)||Intent.ACTION_BATTERY_OKAY.equals(a))refresh(c);}
  @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager am,int id,Bundle b){super.onAppWidgetOptionsChanged(c,am,id,b);updateOne(c,am,id);}
  public static void applyToAll(Context c){
    AppWidgetManager manager=AppWidgetManager.getInstance(c);
    int[] ids=manager.getAppWidgetIds(new ComponentName(c,BatteryWidgetProvider.class));
    SharedPreferences.Editor editor=c.getSharedPreferences("widget_themes",0).edit();
    int selected=ThemeCatalog.load(c);
    for(int id:ids)editor.putInt("theme_"+id,selected);
    editor.apply();
    refreshAsync(c);
  }
  public static void refresh(Context c){AppWidgetManager am=AppWidgetManager.getInstance(c);int[] ids=am.getAppWidgetIds(new ComponentName(c,BatteryWidgetProvider.class));for(int id:ids)updateOne(c,am,id);}
  private static void updateOne(Context c,AppWidgetManager am,int id){
    Bundle opts=am.getAppWidgetOptions(id);float d=c.getResources().getDisplayMetrics().density;
    int w=(int)(Math.max(180,opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,210))*d);
    int h=(int)(Math.max(110,opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,125))*d);
    float factor=Math.min(1f,640f/Math.max(w,h));w=(int)(w*factor);h=(int)(h*factor);
    BatteryState b=BatteryState.read(c);RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.widget_battery);
    SharedPreferences prefs=c.getSharedPreferences("widget_themes",0);
    String key="theme_"+id;
    int style=prefs.getInt(key,-1);
    if(style<0){style=ThemeCatalog.load(c);prefs.edit().putInt(key,style).apply();}
    rv.setImageViewBitmap(R.id.widget_image,BatteryArt.draw(c,w,h,ThemeCatalog.resolve(c,style,b.percent,b.charging),b.percent,b.charging));
    Intent launch=new Intent(c,MainActivity.class);launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pi=PendingIntent.getActivity(c,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    rv.setOnClickPendingIntent(R.id.widget_image,pi);am.updateAppWidget(id,rv);
  }
}
