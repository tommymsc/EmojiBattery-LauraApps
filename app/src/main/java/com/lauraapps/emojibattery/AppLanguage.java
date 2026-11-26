package com.lauraapps.emojibattery;
import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;
public final class AppLanguage {
 private AppLanguage(){}
 public static String selected(Context c){return c.getSharedPreferences("language",0).getString("tag","system");}
 public static void save(Context c,String tag){c.getSharedPreferences("language",0).edit().putString("tag",tag).apply();}
 public static Context wrap(Context c){
  String tag=selected(c);
  if("system".equals(tag))return c;
  Configuration config=new Configuration(c.getResources().getConfiguration());
  config.setLocale(Locale.forLanguageTag(tag));
  return c.createConfigurationContext(config);
 }
}