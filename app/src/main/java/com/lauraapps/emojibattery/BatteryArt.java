package com.lauraapps.emojibattery;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
public final class BatteryArt {
  private BatteryArt(){}
  private static Paint brush(int color){Paint p=new Paint(3);p.setColor(color);return p;}
  public static Bitmap draw(Context c,int w,int h,int theme,int level,boolean charging){
    w=Math.max(160,Math.min(800,w));h=Math.max(100,Math.min(600,h));theme=ThemeCatalog.clamp(theme);
    Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
    Canvas canvas=new Canvas(b);canvas.save();canvas.scale(w/360f,h/220f);
    int fg=ThemeCatalog.INK[theme];
    canvas.drawRoundRect(new RectF(0,0,360,220),28,28,brush(ThemeCatalog.BASE[theme]));
    Paint title=brush(fg);title.setTextSize(19);title.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("emoji battery",24,35,title);
    Paint emoji=brush(fg);emoji.setTextSize(76);canvas.drawText(ThemeCatalog.FACES[theme][BatteryMath.mood(level,charging)],24,135,emoji);
    Paint percent=brush(fg);percent.setTextSize(62);percent.setTypeface(Typeface.DEFAULT_BOLD);
    canvas.drawText(level<0?"--%":level+"%",156,120,percent);
    Paint status=brush(fg);status.setAlpha(180);status.setTextSize(16);canvas.drawText(c.getString(charging?R.string.charging:R.string.battery_level),156,148,status);
    Paint track=brush(fg);track.setAlpha(50);canvas.drawRoundRect(new RectF(23,175,337,193),9,9,track);
    int val=Math.max(0,Math.min(100,level));canvas.drawRoundRect(new RectF(23,175,23+314*val/100f,193),9,9,brush(ThemeCatalog.ACCENT[theme]));
    canvas.restore();return b;
  }
}
