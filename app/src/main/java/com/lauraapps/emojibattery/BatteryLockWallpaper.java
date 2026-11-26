package com.lauraapps.emojibattery;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import java.io.IOException;

/** Explicit, user-triggered, static lock-screen wallpaper. Never runs in background. */
public final class BatteryLockWallpaper {
    private BatteryLockWallpaper(){}
    public static void apply(Context context,int theme) throws IOException {
        WallpaperManager wm=WallpaperManager.getInstance(context);
        if(android.os.Build.VERSION.SDK_INT>=27 && !wm.isWallpaperSupported())throw new IOException("Wallpaper not supported");
        final int w=720,h=1280;
        Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        try{
            Canvas canvas=new Canvas(image);
            Paint gradient=new Paint(3);
            gradient.setShader(new LinearGradient(0,0,w,h,
                new int[]{0xFF101C57,0xFF542E8B,0xFF111A50},
                null,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,gradient);
            Paint glow=new Paint(3);
            glow.setColor(0x4547D6FC);
            canvas.drawCircle(160,660,260,glow);
            glow.setColor(0x4AED60C8);
            canvas.drawCircle(560,930,280,glow);
            BatteryState battery=BatteryState.read(context);
            Bitmap mascot=BatteryArt.draw(context,720,446,theme,battery.percent,battery.charging);
            try{canvas.drawBitmap(mascot,null,new RectF(34,430,686,833),new Paint(3));}finally{mascot.recycle();}
            Paint caption=new Paint(3);
            caption.setColor(0xFFF2E7FF);
            caption.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.BOLD));
            caption.setTextSize(34);
            caption.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(context.getString(R.string.wallpaper_caption),360,890,caption);
            wm.setBitmap(image,null,false,WallpaperManager.FLAG_LOCK);
        }finally{image.recycle();}
    }
}
