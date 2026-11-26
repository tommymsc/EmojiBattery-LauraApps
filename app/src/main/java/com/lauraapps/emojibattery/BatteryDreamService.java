package com.lauraapps.emojibattery;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Opt-in Android system screensaver. Android controls when to start and stop it.
 * Never draws above the lock screen or modifies protected system battery icons.
 */
public final class BatteryDreamService extends DreamService {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private ImageView art;
    private TextView status;
    private ObjectAnimator floatAnimation;
    private boolean listening;
    private int lastTheme=-1,lastPercent=-2;
    private boolean lastCharging;
    private final java.util.concurrent.ExecutorService worker=java.util.concurrent.Executors.newSingleThreadExecutor();
    private int generation;
    private final Runnable refresh=new Runnable(){
        @Override public void run(){
            render();
            handler.postDelayed(this,60000);
        }
    };
    private final BroadcastReceiver batteryChange=new BroadcastReceiver(){
        @Override public void onReceive(Context context, Intent intent){render();}
    };

    @Override protected void attachBaseContext(Context base){super.attachBaseContext(AppLanguage.wrap(base));}
    @Override public void onAttachedToWindow(){
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        LinearLayout screen=new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setGravity(Gravity.CENTER);
        screen.setPadding(22,36,22,36);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF111744,0xFF3E267A,0xFF1A1E52});
        screen.setBackground(bg);
        TextView title=new TextView(this);
        title.setText(getString(R.string.dream_heading));
        title.setTextColor(0xFFF1E9FF);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
        screen.addView(title,new LinearLayout.LayoutParams(-1,-2));
        art=new ImageView(this);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams pic=new LinearLayout.LayoutParams(-1,0,1);
        pic.topMargin=26;
        pic.bottomMargin=18;
        screen.addView(art,pic);
        status=new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(20);
        status.setGravity(Gravity.CENTER);
        screen.addView(status,new LinearLayout.LayoutParams(-1,-2));
        setContentView(screen);
        render();
    }
    @Override public void onDreamingStarted(){
        super.onDreamingStarted();
        if(!listening){
            registerReceiver(batteryChange,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            listening=true;
        }
        handler.removeCallbacks(refresh);
        handler.post(refresh);
        if(art!=null){
            floatAnimation=ObjectAnimator.ofFloat(art,"translationY",-9f,9f);
            floatAnimation.setDuration(4000);
            floatAnimation.setRepeatCount(ValueAnimator.INFINITE);
            floatAnimation.setRepeatMode(ValueAnimator.REVERSE);
            floatAnimation.start();
        }
    }
    private void render(){
        if(art==null)return;
        BatteryState state=BatteryState.read(this);
        int theme=ThemeCatalog.resolve(this,ThemeCatalog.load(this),state.percent,state.charging);
        if(theme==lastTheme && state.percent==lastPercent && state.charging==lastCharging && art.getDrawable()!=null)return;
        final int ticket=++generation;
        worker.execute(()->{android.graphics.Bitmap bmp=BatteryArt.draw(getApplicationContext(),720,446,theme,state.percent,state.charging);handler.post(()->{if(ticket==generation&&art!=null)art.setImageBitmap(bmp);else bmp.recycle();});});
        lastTheme=theme;lastPercent=state.percent;lastCharging=state.charging;
        status.setText(state.percent<0?getString(R.string.unknown):
            (state.charging?getString(R.string.charging):getString(R.string.battery_level))
                +"  •  "+state.percent+"%");
    }
    private void stopUpdates(){
        generation++;lastTheme=-1;
        handler.removeCallbacks(refresh);
        if(floatAnimation!=null){floatAnimation.cancel();floatAnimation=null;}
        if(listening){
            try{unregisterReceiver(batteryChange);}catch(IllegalArgumentException ignored){}
            listening=false;
        }
    }
    @Override public void onDreamingStopped(){stopUpdates();super.onDreamingStopped();}
    @Override public void onDetachedFromWindow(){stopUpdates();art=null;super.onDetachedFromWindow();}
    @Override public void onDestroy(){stopUpdates();worker.shutdownNow();super.onDestroy();}
}
