package com.lauraapps.emojibattery;
import android.content.Context;
public final class ThemeCatalog {
  private ThemeCatalog(){}
  public static final int SPECIES=12,COUNT=24,PALETTES=7,TOTAL=SPECIES*PALETTES;
  // Existing saved IDs 0..83 remain unchanged. New art IDs are append-only.
  public static final int NEW_BASE=1000;
  public static final String[] NEW_MASCOTS={"strawberry","cotton_candy","princess","astronaut","sleepy","rainbow","chocolate","strawberry_original","strawberry_wave","cotton_wave","sleepy_star","astronaut_star","rainbow_cloud","princess_wave"};
  public static final int NEW_COUNT=NEW_MASCOTS.length;
  private static final int[] NEW_DEFAULTS={0,5,1,4,1,5,3,0,0,5,1,4,5,0};
  public static int newIndex(int theme){return theme>=NEW_BASE && theme<NEW_BASE+NEW_COUNT*PALETTES?(theme-NEW_BASE)/PALETTES:-1;}
  public static int newTheme(int index,int palette){return NEW_BASE+Math.max(0,Math.min(NEW_COUNT-1,index))*PALETTES+Math.max(0,Math.min(PALETTES-1,palette));}
  public static int defaultNewTheme(int index){return newTheme(index,NEW_DEFAULTS[index]);}
  public static String assetName(int theme){int i=newIndex(theme);return i<0?null:"mascots/"+NEW_MASCOTS[i]+".webp";}
  public static int palette(int theme){return newIndex(theme)>=0?(theme-NEW_BASE)%PALETTES:Math.max(0,Math.min(TOTAL-1,theme))/SPECIES;}
  public static int withPalette(int theme,int p){
    p=Math.max(0,Math.min(PALETTES-1,p));int i=newIndex(theme);
    return i>=0?newTheme(i,p):p*SPECIES+clamp(theme)%SPECIES;
  }
  public static boolean gallerySelected(int card,int selected){
    int i=newIndex(card);
    return i>=0?newIndex(selected)==i:newIndex(selected)<0&&(card==selected||(selected>=COUNT&&card==selected%SPECIES));
  }
  public static int accent(int theme){return new int[]{0xFFFF5C94,0xFF9D71E0,0xFF22C9A0,0xFFFFA54D,0xFF4C9DFF,0xFFEF78C9,0xFFB8B9DE}[palette(theme)];}
  public static int background(int theme){return new int[]{0xFF49205F,0xFF302779,0xFF164F50,0xFF633D35,0xFF193C72,0xFF732B66,0xFF242637}[palette(theme)];}
  public static final int[] BASE={0xFFFFE3F0,0xFFEBE1FF,0xFFE8FFEE,0xFFFFEBD8,0xFFE4F2FF,0xFFF3E7FF,0xFFFFEDF2,0xFFFFF3DA,0xFFE2FFF7,0xFFE8E7FF,0xFFFFF2E3,0xFFE7F0FF,0xFFFFEAF0,0xFFF1E7FF,0xFFE6F9F2,0xFFFFF0E8,0xFFEAF6FF,0xFFF5EEFF,0xFFFFF0EE,0xFFFFF6E8,0xFFE5FCF7,0xFFF0E9FF,0xFFFFF0DC,0xFFE4F4FF};
  public static final int[] ACCENT={0xFFFF5C94,0xFF8C55E9,0xFF33BC7C,0xFFF08D3D,0xFF449DED,0xFF8E6AEA,0xFFE961A0,0xFFE8A63A,0xFF34B6AF,0xFF7965D5,0xFFF69958,0xFF3994E6,0xFFE85B86,0xFF9D71E0,0xFF29B98A,0xFFEF8650,0xFF308FE6,0xFFA077E3,0xFFF06C92,0xFFE3A82E,0xFF1CA9A2,0xFF8664DD,0xFFEA8C4A,0xFF3D93C7};
  public static final int[] INK={0xFF593850,0xFF493768,0xFF2F5947,0xFF674330,0xFF324C6B,0xFF493A6B,0xFF593850,0xFF493768,0xFF2F5947,0xFF674330,0xFF324C6B,0xFF493A6B,0xFF593850,0xFF493768,0xFF2F5947,0xFF674330,0xFF324C6B,0xFF493A6B,0xFF593850,0xFF493768,0xFF2F5947,0xFF674330,0xFF324C6B,0xFF493A6B};
  public static final int[] BODY={0xFFFFE1A8,0xFFC6A0FA,0xFFB5E58A,0xFFFFCAA1,0xFF7CBDF7,0xFFB9A3E9,0xFFFFF3F3,0xFFFFC98B,0xFFB7F6E0,0xFFCAB8F5,0xFFFFE2A5,0xFF98D4F9,0xFFF4CCD7,0xFFBBA8FB,0xFF8DE0AC,0xFFF9B894,0xFF77B6E4,0xFFA68CE6,0xFFF5CEBB,0xFFEED4A1,0xFFAFE7D8,0xFFD9BEFF,0xFFFFD38F,0xFF8FC8F3};
  public static final int[] SECONDARY={0xFFFF95C5,0xFFF5A9EB,0xFF69C15B,0xFFB97A58,0xFFFFFFFF,0xFF5EE7F4,0xFFE987B0,0xFFE0A65A,0xFF62CDAD,0xFFB1A5F4,0xFFE2BB8A,0xFFA4E8FF,0xFFF097BE,0xFFD9A3EC,0xFF6BC49B,0xFFD7A17A,0xFFFFFFFF,0xFF6ED9F1,0xFFE9A1B0,0xFFBA905E,0xFF6ECCB4,0xFFC5A0ED,0xFFFFD86D,0xFF8FC2EE};
  public static int clamp(int v){return newIndex(v)>=0?v:Math.max(0,Math.min(TOTAL-1,v));}
  public static int load(Context c){return clamp(c.getSharedPreferences("style",0).getInt("theme",0));}
  public static void save(Context c,int v){c.getSharedPreferences("style",0).edit().putInt("theme",clamp(v)).apply();}
  public static boolean isAuto(Context c){return c.getSharedPreferences("style",0).getBoolean("auto_mode",false);}
  public static void setAuto(Context c,boolean enabled){c.getSharedPreferences("style",0).edit().putBoolean("auto_mode",enabled).apply();}
  public static int resolve(Context c,int fixedTheme,int level,boolean charging){
    return isAuto(c)&&level>=0?BatteryMath.automaticTheme(level,charging,fixedTheme):clamp(fixedTheme);
  }
  public static int species(int theme){return new int[]{8,6,7,3,5,11,0,9,2,1,4,10}[(newIndex(theme)>=0?6:clamp(theme))%SPECIES];}
}
