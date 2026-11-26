package com.lauraapps.emojibattery;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.provider.Settings;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private final int WHITE=0xFFFFFFFF, MUTED=0xFFC5C7EE, LILAC=0xFFBFA9FF;
    private final int[] names={R.string.mascot_0,R.string.mascot_1,R.string.mascot_2,R.string.mascot_3,R.string.mascot_4,R.string.mascot_5,R.string.mascot_6,R.string.mascot_7,R.string.mascot_8,R.string.mascot_9,R.string.mascot_10,R.string.mascot_11,R.string.mascot_12,R.string.mascot_13,R.string.mascot_14,R.string.mascot_15,R.string.mascot_16,R.string.mascot_17,R.string.mascot_18,R.string.mascot_19,R.string.mascot_20,R.string.mascot_21,R.string.mascot_22,R.string.mascot_23};
    private final int[] newNames={R.string.cat_strawberry,R.string.cat_cotton_candy,R.string.cat_princess,R.string.cat_astronaut,R.string.cat_sleepy,R.string.cat_rainbow,R.string.cat_chocolate,R.string.cat_strawberry_original,R.string.cat_strawberry_wave,R.string.cat_cotton_wave,R.string.cat_sleepy_star,R.string.cat_astronaut_star,R.string.cat_rainbow_cloud,R.string.cat_princess_wave,R.string.dog_astronaut,R.string.dog_chocolate,R.string.dog_cotton_candy,R.string.dog_princess,R.string.dog_rainbow,R.string.dog_sleepy_moon,R.string.dog_strawberry,R.string.bear_polar,R.string.bear_teddy,R.string.panda_rainbow,R.string.koala_leaf,R.string.bear_honey,R.string.panda_red,R.string.fruit_avocado,R.string.fruit_grapes,R.string.fruit_orange,R.string.fruit_peach,R.string.fruit_pineapple,R.string.fruit_strawberry,R.string.fruit_watermelon,R.string.fantasy_ghost,R.string.fantasy_unicorn,R.string.fantasy_wizard,R.string.fantasy_dragon,R.string.fantasy_phoenix,R.string.fantasy_fairy,R.string.food_pizza,R.string.food_icecream,R.string.food_donut,R.string.food_boba,R.string.food_burger,R.string.food_cupcake,R.string.food_sushi,R.string.aquatic_dolphin,R.string.aquatic_jellyfish,R.string.aquatic_octopus,R.string.aquatic_shark,R.string.aquatic_tropical_fish,R.string.aquatic_turtle,R.string.space_alien,R.string.space_astronaut,R.string.space_comet,R.string.space_moon,R.string.space_planet,R.string.space_rocket,R.string.space_star,R.string.emotion_love,R.string.emotion_crystal,R.string.emotion_laugh,R.string.emotion_rainbow,R.string.emotion_sleepy,R.string.emotion_cool,R.string.emotion_hug,R.string.nature_sun,R.string.nature_blossom,R.string.nature_snow};
    private int selection=0,filter=0,previewValue=-1;
    private int galleryLimit=16;
    private boolean editing=false;
    private int galleryScrollY=0;
    private String searchText="";
    private TextView galleryCount;
    private final Handler searchHandler=new Handler(Looper.getMainLooper());
    private final Runnable searchRefresh=this::refreshSearchResults;
    private void refreshSearchResults(){if(!editing&&gallery!=null){galleryLimit=16;renderGallery();}}
    private TextView widgetAction,widgetCountNotice;
    private static final long PIN_COOLDOWN_MS=60000L;
    private LinearLayout modePanel,levelControls;
    private final android.util.LruCache<String,android.graphics.Bitmap> thumbnails=new android.util.LruCache<String,android.graphics.Bitmap>(8*1024*1024){@Override protected int sizeOf(String k,android.graphics.Bitmap v){return v.getAllocationByteCount();}};
    private final android.util.LruCache<String,android.graphics.Bitmap> previews=new android.util.LruCache<String,android.graphics.Bitmap>(5*1024*1024){@Override protected int sizeOf(String k,android.graphics.Bitmap v){return v.getAllocationByteCount();}};
    private String requestedPreview="";
    private View customAnchor;
    private final int[] paletteNames={R.string.palette_0,R.string.palette_1,R.string.palette_2,R.string.palette_3,R.string.palette_4,R.string.palette_5,R.string.palette_6};
    private ScrollView scroll;
    private LinearLayout root;
    private LinearLayout gallery,filters;
    private ImageView heroArt;
    private TextView heroStatus,selectionLabel;
    private View galleryAnchor;
    private ObjectAnimator pulse;
    private boolean listening=false;
    private final java.util.concurrent.ThreadPoolExecutor renderWorker=new java.util.concurrent.ThreadPoolExecutor(1,1,0L,java.util.concurrent.TimeUnit.MILLISECONDS,new java.util.concurrent.LinkedBlockingQueue<>());
    private final ExecutorService wallpaperWorker=Executors.newSingleThreadExecutor();
    private final java.util.concurrent.ThreadPoolExecutor previewWorker=new java.util.concurrent.ThreadPoolExecutor(1,1,0L,java.util.concurrent.TimeUnit.MILLISECONDS,new java.util.concurrent.LinkedBlockingQueue<>());
    private final Handler uiHandler=new Handler(Looper.getMainLooper());
    private volatile int galleryGeneration=0;
    private volatile int previewGeneration=0;
    private final BroadcastReceiver live=new BroadcastReceiver(){
        @Override public void onReceive(Context context,Intent intent){if(previewValue==-1)updatePreview();BatteryWidgetProvider.refreshAsync(context);}
    };
    @Override protected void attachBaseContext(Context base){super.attachBaseContext(AppLanguage.wrap(base));}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        selection=ThemeCatalog.load(this);
        if(state!=null){galleryScrollY=state.getInt("galleryScrollY",0);searchText=state.getString("searchText","");editing=state.getBoolean("editing");filter=state.getInt("filter",0);galleryLimit=state.getInt("galleryLimit",16);previewValue=state.getInt("previewValue",-1);selection=ThemeCatalog.clamp(state.getInt("selection",selection));}
        build();syncBackNavigation();
        if(state!=null){int y=state.getInt("scrollY",0);scroll.post(()->scroll.scrollTo(0,y));}
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putInt("galleryScrollY",galleryScrollY);out.putString("searchText",searchText);out.putBoolean("editing",editing);out.putInt("filter",filter);out.putInt("galleryLimit",galleryLimit);out.putInt("previewValue",previewValue);out.putInt("selection",selection);out.putInt("scrollY",scroll==null?0:scroll.getScrollY());super.onSaveInstanceState(out);
    }
    @Override protected void onResume(){
        super.onResume();
        if(!listening){
            registerReceiver(live,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            listening=true;
        }
        updatePreview();
        updateWidgetAction();
        BatteryWidgetProvider.refreshAsync(this);
        if(pulse!=null && !pulse.isStarted()) pulse.start();
    }
    @Override protected void onPause(){
        if(pulse!=null)pulse.cancel();
        if(listening){unregisterReceiver(live);listening=false;}
        super.onPause();
    }
    @Override protected void onDestroy(){
        searchHandler.removeCallbacksAndMessages(null);
        galleryGeneration++;
        previewGeneration++;
        renderWorker.shutdownNow();
        wallpaperWorker.shutdown();
        previewWorker.shutdownNow();
        uiHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable gradient(int[] colors,int radius,int stroke){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,colors);
        d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1.5f),stroke);
        return d;
    }
    private GradientDrawable solid(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String value,int size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(value);v.setTextColor(color);v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
        return v;
    }
    private LinearLayout col(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;
    }
    private LinearLayout row(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;
    }
    private LinearLayout.LayoutParams lp(int w,int h,int top){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));
        p.topMargin=dp(top);return p;
    }
    private TextView action(String name,boolean primary,int icon){
        TextView b=text((icon==0?"":Character.toString((char)icon)+"  ")+name+"    ›",16,primary?WHITE:0xFF33305E,true);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(dp(19),0,dp(12),0);
        b.setBackground(primary
            ?gradient(new int[]{0xFFFF7D59,0xFFFA469D,0xFF8747FF},20,0xFFFDCEFF)
            :gradient(new int[]{0xFFFFFFFF,0xFFE4D9FF},20,0xFFD7BEFF));
        b.setElevation(dp(4));
        b.setFocusable(true);
        return b;
    }
    private void build(){
        if(pulse!=null){pulse.cancel();pulse=null;}
        if(Build.VERSION.SDK_INT<35){
            getWindow().setStatusBarColor(0xFF111A53);
            getWindow().setNavigationBarColor(0xFF10163F);
        }
        scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setSmoothScrollingEnabled(true);
        galleryGeneration++;
        renderWorker.getQueue().clear();
        scroll.setBackground(gradient(new int[]{0xFF101B57,0xFF392274,0xFF161B52},0,0));
        root=col();
        root.setPadding(dp(16),dp(19),dp(16),dp(45));
        scroll.addView(root);
        setContentView(scroll);
        if(Build.VERSION.SDK_INT>=35){
            scroll.setOnApplyWindowInsetsListener((v,i)->{
                android.graphics.Insets e=i.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                root.setPadding(dp(16)+e.left,dp(14)+e.top,dp(16)+e.right,dp(40)+e.bottom);
                return i;
            });
            scroll.requestApplyInsets();
        }
        widgetAction=null;widgetCountNotice=null;
        heroArt=null;requestedPreview="";previewGeneration++;previewWorker.getQueue().clear();
        drawHeader();
        if(editing){
            TextView back=action(getString(R.string.back_gallery),false,0);root.addView(back,lp(-1,48,6));back.setContentDescription("back_to_gallery");back.setOnClickListener(v->showGallery());
            drawHero();drawModeSelector();drawCustomize();drawScreenTools();
        }else{
            TextView edit=action(getString(R.string.edit_selected),true,0);root.addView(edit,lp(-1,48,8));edit.setContentDescription("edit_selected_mascot");edit.setOnClickListener(v->showEditor());
            drawGallery();drawFooter();
        }
        scroll.post(()->scroll.scrollTo(0,0));
        updatePreview();
        if(heroArt!=null){
            pulse=ObjectAnimator.ofFloat(heroArt,"alpha",1.0f,0.97f,1.0f);
            pulse.setDuration(2300);
            pulse.setRepeatCount(0);
            pulse.start();
        }
    }
    private void showEditor(){galleryScrollY=scroll.getScrollY();searchHandler.removeCallbacks(searchRefresh);editing=true;previewValue=-1;build();syncBackNavigation();}
    private void showGallery(){editing=false;build();syncBackNavigation();scroll.post(()->scroll.scrollTo(0,galleryScrollY));}
    private Object backCallback;
    private void syncBackNavigation(){
        if(Build.VERSION.SDK_INT>=33)syncModernBack();
    }
    @android.annotation.TargetApi(33)
    private void syncModernBack(){
        if(backCallback!=null){
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((android.window.OnBackInvokedCallback)backCallback);
            backCallback=null;
        }
        if(editing){
            android.window.OnBackInvokedCallback callback=this::showGallery;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,callback);
            backCallback=callback;
        }
    }
    @Override public boolean onKeyDown(int keyCode,android.view.KeyEvent event){
        if(Build.VERSION.SDK_INT<33&&editing&&keyCode==android.view.KeyEvent.KEYCODE_BACK){event.startTracking();return true;}
        return super.onKeyDown(keyCode,event);
    }
    @Override public boolean onKeyUp(int keyCode,android.view.KeyEvent event){
        if(Build.VERSION.SDK_INT<33&&editing&&keyCode==android.view.KeyEvent.KEYCODE_BACK&&event.isTracking()&&!event.isCanceled()){showGallery();return true;}
        return super.onKeyUp(keyCode,event);
    }
    private void drawHeader(){
        LinearLayout top=row();
        root.addView(top,lp(-1,38,0));
        TextView brand=text("✦  LAURAAPPS    /    DIY STUDIO",12,0xFFB5DEFF,true);
        top.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        TextView lang=text("🌐 "+getString(R.string.language_button),12,0xFFFFE394,true);
        lang.setPadding(dp(8),0,dp(8),0);lang.setMinHeight(dp(48));lang.setFocusable(true);lang.setContentDescription("language_selector");
        top.addView(lang);lang.setOnClickListener(v->chooseLanguage());
        TextView star=text("✦  ✧  ✨",26,0xFFFFD86E,true);
        star.setGravity(Gravity.CENTER);
        if(!editing)root.addView(star,lp(-1,28,3));
        TextView title=text("Emoji Battery:",29,WHITE,true);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(6,0,3,0xFF8854F4);
        root.addView(title,lp(-1,-2,0));
        TextView title2=text("DIY Widgets",32,0xFFFFD354,true);
        title2.setGravity(Gravity.CENTER);
        title2.setShadowLayer(5,0,3,0xFFDD5789);
        if(!editing)root.addView(title2,lp(-1,-2,0));
        TextView subtitle=text(getString(R.string.hero_subtitle),14,0xFFE5DAFF,false);
        subtitle.setGravity(Gravity.CENTER);
        if(!editing)root.addView(subtitle,lp(-1,-2,5));
        TextView steps=text("① "+getString(R.string.steps_choose)+"   ② "+getString(R.string.steps_preview)+"   ③ "+getString(R.string.steps_add),12,0xFFFFF3C5,true);
        steps.setGravity(Gravity.CENTER);
        if(!editing)root.addView(steps,lp(-1,28,4));
    }
    private void chooseLanguage(){
        String current=AppLanguage.selected(this);
        String[] tags={"system","es","en"};
        String[] labels={getString(R.string.language_system),"Español","English"};
        int selected="es".equals(current)?1:"en".equals(current)?2:0;
        new AlertDialog.Builder(this).setTitle(R.string.language_button).setSingleChoiceItems(labels,selected,(dialog,which)->{
            AppLanguage.save(this,tags[which]);dialog.dismiss();
            BatteryWidgetProvider.refreshAsync(this);recreate();
        }).setNegativeButton(android.R.string.cancel,null).show();
    }
    private void drawHero(){
        LinearLayout panel=col();
        panel.setPadding(dp(10),dp(10),dp(10),dp(12));
        panel.setBackground(gradient(new int[]{0xFF172B7A,0xFF5431A7,0xFFB656B6},24,0xFF8FE2FF));
        panel.setElevation(dp(6));
        root.addView(panel,lp(-1,-2,19));
        TextView tag=text("✦    "+getString(R.string.hero_tag)+"    ✦",12,0xFFFFE7B9,true);
        tag.setGravity(Gravity.CENTER);
        panel.addView(tag,lp(-1,24,3));
        heroArt=new ImageView(this);
        heroArt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        heroArt.setAdjustViewBounds(true);
        panel.addView(heroArt,lp(-1,154,1));
        heroStatus=text("",13,0xFFFFF2FF,true);
        heroStatus.setGravity(Gravity.CENTER);
        heroStatus.setContentDescription("battery_status");
        panel.addView(heroStatus,lp(-1,-2,0));
        TextView real=action(getString(R.string.real_battery_action),false,0);
        real.setOnClickListener(v->{previewValue=-1;refreshLevels();updatePreview();});
        panel.addView(real,lp(-1,48,8));
        TextView helpGuide=text(getString(R.string.battery_guide_short),12,WHITE,false);panel.addView(helpGuide,lp(-1,-2,5));helpGuide.setOnClickListener(v->new AlertDialog.Builder(this).setMessage(R.string.battery_guide).setPositiveButton(android.R.string.ok,null).show());
        widgetAction=action(getString(R.string.button_add),true,0);
        widgetAction.setOnClickListener(v->pin());
        panel.addView(widgetAction,lp(-1,55,12));
        widgetCountNotice=text("",12,WHITE,false);
        panel.addView(widgetCountNotice,lp(-1,-2,5));
        updateWidgetAction();
        TextView help=text(getString(R.string.widget_help),11,0xFFE5DBFF,false);
        help.setGravity(Gravity.CENTER);
        panel.addView(help,lp(-1,-2,6));
    }
    private void drawMenu(){
        LinearLayout area=col();
        root.addView(area,lp(-1,-2,17));
        TextView explore=action(getString(R.string.button_gallery),true,0);
        area.addView(explore,lp(-1,55,0));
        explore.setOnClickListener(v->jumpTo(galleryAnchor));
        TextView personalize=action(getString(R.string.customize_title),false,0);
        area.addView(personalize,lp(-1,52,10));
        personalize.setOnClickListener(v->jumpTo(customAnchor));
        TextView apply=action(getString(R.string.button_apply),false,0);
        area.addView(apply,lp(-1,52,10));
        apply.setOnClickListener(v->applyExisting());
    }
    private void drawModeSelector(){
        LinearLayout panel=col();
        panel.setPadding(dp(14),dp(13),dp(14),dp(15));
        panel.setBackground(gradient(new int[]{0xFF243578,0xFF543B9C},20,0xFF89B5FF));
        modePanel=panel;
        root.addView(panel,lp(-1,-2,8));
        fillModes();
    }
    private void fillModes(){
        LinearLayout panel=modePanel;panel.removeAllViews();
        panel.addView(text(getString(R.string.mode_heading),18,WHITE,true));
        boolean auto=ThemeCatalog.isAuto(this);
        TextView favorite=action((auto?"○  ":"✓  ")+getString(R.string.mode_fixed),!auto,0);
        favorite.setContentDescription("fixed_battery_mode");favorite.setOnClickListener(v->setMascotMode(false));
        panel.addView(favorite,lp(-1,50,12));
        TextView automatic=action((auto?"✓  ":"○  ")+getString(R.string.mode_auto),auto,0);
        automatic.setContentDescription("automatic_battery_mode");automatic.setOnClickListener(v->setMascotMode(true));
        panel.addView(automatic,lp(-1,50,9));
        panel.addView(text(getString(auto?R.string.mode_auto_desc:R.string.mode_fixed_desc),
            12,0xFFEBE5FF,false),lp(-1,-2,11));
    }
    private void setMascotMode(boolean auto){
        if(ThemeCatalog.isAuto(this)==auto)return;
        ThemeCatalog.setAuto(this,auto);
        BatteryWidgetProvider.refreshAsync(this);
        fillModes();updatePreview();
    }
    private void drawScreenTools(){
        LinearLayout panel=col();
        panel.setPadding(dp(14),dp(14),dp(14),dp(16));
        panel.setBackground(gradient(new int[]{0xFF1B3779,0xFF56329A},22,0xFF82BFFF));
        root.addView(panel,lp(-1,-2,19));
        TextView header=text(getString(R.string.screen_tools_title),18,WHITE,true);
        panel.addView(header,lp(-1,-2,0));
        TextView explain=text(getString(R.string.screen_tools_desc),12,MUTED,false);
        panel.addView(explain,lp(-1,-2,8));
        TextView dream=action(getString(R.string.open_dream_settings),false,0);
        dream.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(R.string.dream_name).setMessage(R.string.dream_guide).setPositiveButton(R.string.open_dream_settings,(dialog,which)->openDreamSettings()).setNegativeButton(android.R.string.cancel,null).show());
        panel.addView(dream,lp(-1,50,14));
        TextView demo=action(getString(R.string.dream_preview),false,0);
        demo.setOnClickListener(v->previewDream());panel.addView(demo,lp(-1,50,9));
        TextView lock=action(getString(R.string.set_lock_wallpaper),false,0);
        lock.setOnClickListener(v->confirmLockWallpaper());
        panel.addView(lock,lp(-1,50,9));
        TextView info=text(getString(R.string.lock_static_notice),11,0xFFE4D7FF,false);
        panel.addView(info,lp(-1,-2,11));
    }
    private void previewDream(){
        BatteryState state=BatteryState.read(this);
        ImageView image=new ImageView(this);image.setAdjustViewBounds(true);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setImageBitmap(BatteryArt.draw(this,720,446,ThemeCatalog.resolve(this,selection,state.percent,state.charging),state.percent,state.charging));
        new AlertDialog.Builder(this).setTitle(R.string.dream_preview).setView(image).setMessage(R.string.dream_preview_notice).setPositiveButton(android.R.string.ok,null).show();
    }
    private void openDreamSettings(){
        try{
            startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS));
        }catch(Exception ex){
            new AlertDialog.Builder(this)
                .setTitle(R.string.dream_name)
                .setMessage(R.string.dream_unavailable)
                .setPositiveButton(android.R.string.ok,null).show();
        }
    }
    private void confirmLockWallpaper(){
        new AlertDialog.Builder(this).setTitle(R.string.wallpaper_confirm_title)
            .setMessage(R.string.wallpaper_confirm_message)
            .setNegativeButton(android.R.string.cancel,null)
            .setPositiveButton(R.string.apply_wallpaper,(dialog,which)->{
                try{
                    BatteryState state=BatteryState.read(this);
                    final int theme=ThemeCatalog.resolve(this,selection,state.percent,state.charging);
                    wallpaperWorker.execute(()->{
                        try{
                            BatteryLockWallpaper.apply(getApplicationContext(),theme);
                            uiHandler.post(()->{if(!isDestroyed()&&!isFinishing())Toast.makeText(this,R.string.wallpaper_applied,Toast.LENGTH_LONG).show();});
                        }catch(Exception ex){
                            uiHandler.post(()->{if(!isDestroyed()&&!isFinishing())new AlertDialog.Builder(this).setTitle(R.string.wallpaper_confirm_title).setMessage(R.string.wallpaper_failed).setPositiveButton(android.R.string.ok,null).show();});
                        }
                    });
                }catch(Exception ex){
                    new AlertDialog.Builder(this).setTitle(R.string.wallpaper_confirm_title)
                        .setMessage(R.string.wallpaper_failed)
                        .setPositiveButton(android.R.string.ok,null).show();
                }
            }).show();
    }
    private void jumpTo(View target){
        if(target==null)return;
        scroll.post(()->scroll.smoothScrollTo(0,Math.max(0,target.getTop()-dp(20))));
    }
    private void drawGallery(){
        galleryAnchor=text("✦  "+getString(R.string.section_explore),23,WHITE,true);
        root.addView(galleryAnchor,lp(-1,-2,26));
        TextView sub=text(getString(R.string.gallery_subtitle),13,MUTED,false);
        root.addView(sub,lp(-1,-2,4));
        filters=row();
        android.widget.HorizontalScrollView categoryScroll=new android.widget.HorizontalScrollView(this);
        categoryScroll.setHorizontalScrollBarEnabled(false);
        categoryScroll.setFillViewport(false);
        categoryScroll.addView(filters,new android.widget.FrameLayout.LayoutParams(-2,-1));
        root.addView(categoryScroll,lp(-1,48,13));
        categoryScroll.post(()->{if(filter<filters.getChildCount()){View selected=filters.getChildAt(filter);categoryScroll.scrollTo(Math.max(0,selected.getLeft()-dp(12)),0);}});
        addFilter(getString(R.string.filter_all),0);
        addFilter(getString(R.string.filter_signature),1);
        addFilter(getString(R.string.filter_pastel),2);
        addFilter(getString(R.string.filter_cats),3);
        addFilter(getString(R.string.filter_dogs),4);
        addFilter(getString(R.string.filter_bears),5);
        addFilter(getString(R.string.filter_fruits),6);
        addFilter(getString(R.string.filter_fantasy),7);
        addFilter(getString(R.string.filter_food),8);
        addFilter(getString(R.string.filter_aquatic),9);
        addFilter(getString(R.string.filter_space),10);
        addFilter(getString(R.string.filter_emotions),11);
        addFilter(getString(R.string.filter_nature),12);
        android.widget.EditText search=new android.widget.EditText(this);
        search.setSingleLine(true);search.setTextColor(WHITE);search.setHintTextColor(MUTED);search.setTextSize(14);search.setPadding(dp(14),0,dp(14),0);
        search.setBackground(solid(0xFF30276C,14));search.setHint(R.string.search_mascots);search.setText(searchText);
        search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener((v,action,event)->{((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);search.clearFocus();return true;});
        root.setFocusableInTouchMode(true);root.requestFocus();
        root.addView(search,lp(-1,48,8));
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){searchText=s.toString();searchHandler.removeCallbacks(searchRefresh);searchHandler.postDelayed(searchRefresh,180);}public void afterTextChanged(android.text.Editable e){}});
        galleryCount=text("",12,MUTED,false);root.addView(galleryCount,lp(-1,-2,5));
        gallery=col();
        root.addView(gallery,lp(-1,-2,8));
        renderGallery();
    }
    private void addFilter(String name,int index){
        TextView label=text(name,12,filter==index?WHITE:0xFFE5DFFF,true);
        label.setGravity(Gravity.CENTER);
        label.setBackground(filter==index
          ?gradient(new int[]{0xFF06BEFE,0xFF8555FF},17,0xFFADEBFF)
          :gradient(new int[]{0xFF293B80,0xFF423475},17,0xFF655BB6));
        label.setPadding(dp(14),0,dp(14),0);
        label.setMinWidth(dp(80));
        label.setSingleLine(true);
        label.setFocusable(true);
        label.setSelected(filter==index);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(44));
        p.rightMargin=dp(6);filters.addView(label,p);
        label.setOnClickListener(v->{if(filter==index)return;filter=index;galleryLimit=16;refreshFilters();renderGallery();});
    }
    private void refreshFilters(){for(int i=0;i<filters.getChildCount();i++){TextView v=(TextView)filters.getChildAt(i);boolean selected=i==filter;v.setSelected(selected);v.setTextColor(selected?WHITE:0xFFE5DFFF);v.setBackground(selected?gradient(new int[]{0xFF06BEFE,0xFF8555FF},17,0xFFA2E6FF):gradient(new int[]{0xFF293B80,0xFF423475},17,0xFF655BB6));}}
    private void renderGallery(){
        galleryGeneration++;renderWorker.getQueue().clear();gallery.removeAllViews();
        final int start=filter==12?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT+ThemeCatalog.FRUIT_COUNT+ThemeCatalog.FANTASY_COUNT+ThemeCatalog.FOOD_COUNT+ThemeCatalog.AQUATIC_COUNT+ThemeCatalog.SPACE_COUNT+ThemeCatalog.EMOTION_COUNT:filter==11?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT+ThemeCatalog.FRUIT_COUNT+ThemeCatalog.FANTASY_COUNT+ThemeCatalog.FOOD_COUNT+ThemeCatalog.AQUATIC_COUNT+ThemeCatalog.SPACE_COUNT:filter==10?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT+ThemeCatalog.FRUIT_COUNT+ThemeCatalog.FANTASY_COUNT+ThemeCatalog.FOOD_COUNT+ThemeCatalog.AQUATIC_COUNT:filter==9?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT+ThemeCatalog.FRUIT_COUNT+ThemeCatalog.FANTASY_COUNT+ThemeCatalog.FOOD_COUNT:filter==8?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT+ThemeCatalog.FRUIT_COUNT+ThemeCatalog.FANTASY_COUNT:filter==7?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT+ThemeCatalog.FRUIT_COUNT:filter==6?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT+ThemeCatalog.BEAR_COUNT:filter==5?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT+ThemeCatalog.DOG_COUNT:filter==4?ThemeCatalog.COUNT+ThemeCatalog.KITTEN_COUNT:filter==3?ThemeCatalog.COUNT:filter==2?12:0;
        final int count=filter==0?ThemeCatalog.COUNT+ThemeCatalog.NEW_COUNT:filter==3?ThemeCatalog.KITTEN_COUNT:filter==4?ThemeCatalog.DOG_COUNT:filter==5?ThemeCatalog.BEAR_COUNT:filter==6?ThemeCatalog.FRUIT_COUNT:filter==7?ThemeCatalog.FANTASY_COUNT:filter==8?ThemeCatalog.FOOD_COUNT:filter==9?ThemeCatalog.AQUATIC_COUNT:filter==10?ThemeCatalog.SPACE_COUNT:filter==11?ThemeCatalog.EMOTION_COUNT:filter==12?ThemeCatalog.NATURE_COUNT:12;
        java.util.ArrayList<Integer> matches=new java.util.ArrayList<>();
        String query=normalizeSearch(searchText);
        for(int i=start;i<start+count;i++){
            String name=getString(i<ThemeCatalog.COUNT?names[i]:newNames[i-ThemeCatalog.COUNT]);
            if(query.isEmpty()||normalizeSearch(name).contains(query))matches.add(i);
        }
        int totalMatches=matches.size();
        final int visibleCount=Math.min(totalMatches,galleryLimit);
        if(galleryCount!=null)galleryCount.setText(getString(R.string.gallery_result_count,visibleCount,totalMatches));
        if(totalMatches==0){gallery.addView(text(getString(R.string.search_empty),14,WHITE,false),lp(-1,-2,12));return;}
        // Gallery artwork is a stable sample, not a live battery display.
        final int sampleLevel=70;
        for(int line=0;line<(visibleCount+1)/2;line++){
            LinearLayout horizontal=row();
            gallery.addView(horizontal,lp(-1,156,10));
            for(int col=0;col<2;col++){
                if(line*2+col>=visibleCount){
                    View empty=new View(this);horizontal.addView(empty,new LinearLayout.LayoutParams(0,-1,1));break;
                }
                final int catalogIndex=matches.get(line*2+col);
                final int index=catalogIndex<ThemeCatalog.COUNT?catalogIndex:ThemeCatalog.defaultNewTheme(catalogIndex-ThemeCatalog.COUNT);
                final boolean chosen=ThemeCatalog.gallerySelected(index,selection);
                final int shownTheme=chosen?selection:index;
                LinearLayout item=col();
                item.setPadding(dp(5),dp(6),dp(5),dp(9));
                item.setBackground(gradient(new int[]{0xFF222A79,0xFF39226E},17,chosen?0xFF6CEFFF:0xFF7558CB));
                item.setElevation(chosen?dp(5):0);
                LinearLayout.LayoutParams part=new LinearLayout.LayoutParams(0,-1,1);
                if(col==1)part.leftMargin=dp(9);
                horizontal.addView(item,part);
                ImageView image=new ImageView(this);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                final int generation=galleryGeneration;
                final int theme=shownTheme, level=sampleLevel;
                final boolean charging=false;
                final String thumbKey=theme+":"+level+":"+charging;
                android.graphics.Bitmap hit=thumbnails.get(thumbKey);
                if(hit!=null)image.setImageBitmap(hit);
                else renderWorker.execute(()->{
                    if(Thread.currentThread().isInterrupted() || generation!=galleryGeneration)return;
                    android.graphics.Bitmap bmp=BatteryArt.draw(getApplicationContext(),240,150,theme,level,charging);
                    thumbnails.put(thumbKey,bmp);
                    uiHandler.post(()->{
                        if(!isFinishing()&&!isDestroyed()&&generation==galleryGeneration)image.setImageBitmap(bmp);
                    });
                });
                item.addView(image,lp(-1,111,0));
                TextView label=text(getString(catalogIndex<ThemeCatalog.COUNT?names[catalogIndex]:newNames[catalogIndex-ThemeCatalog.COUNT])+(chosen?"  ✓":""),11,WHITE,true);
                label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);label.setGravity(Gravity.CENTER);
                item.addView(label,lp(-1,32,0));
                item.setFocusable(true);
                item.setContentDescription(label.getText());
                item.setOnClickListener(v->{
                    selection=shownTheme;
                    ThemeCatalog.save(this,selection);
                    ThemeCatalog.setAuto(this,false);
                    BatteryWidgetProvider.applyToAll(this);
                    showEditor();
                });
            }
        }
        if(visibleCount<totalMatches){
            TextView more=action(getString(R.string.gallery_more,visibleCount,totalMatches),false,0);
            more.setOnClickListener(v->{galleryLimit+=16;renderGallery();});
            gallery.addView(more,lp(-1,52,12));
        }
    }
    private String normalizeSearch(String value){return java.text.Normalizer.normalize(value.trim().toLowerCase(java.util.Locale.ROOT),java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","");}
    private void refreshChoice(){
        int y=scroll.getScrollY();
        build();
        scroll.post(()->scroll.scrollTo(0,y));
    }
    private void drawCustomize(){
        LinearLayout section=col();
        customAnchor=section;
        section.setPadding(dp(13),dp(13),dp(13),dp(13));
        section.setBackground(gradient(new int[]{0xFF282377,0xFF423390},20,0xFF8863DE));
        root.addView(section,lp(-1,-2,24));
        int ni=ThemeCatalog.newIndex(selection);
        selectionLabel=text(getString(ni>=0?newNames[ni]:names[selection%ThemeCatalog.SPECIES]),19,WHITE,true);
        selectionLabel.setContentDescription("selected_mascot_"+selection);
        section.addView(selectionLabel);
        section.addView(text(getString(R.string.palette_title),16,WHITE,true),lp(-1,-2,12));
        final TextView[] swatches=new TextView[ThemeCatalog.PALETTES];
        for(int start=0;start<ThemeCatalog.PALETTES;start+=3){
            LinearLayout colors=row();
            section.addView(colors,lp(-1,39,5));
            for(int i=start;i<Math.min(start+3,ThemeCatalog.PALETTES);i++){
                final int palette=i;
                final int theme=ThemeCatalog.withPalette(selection,palette);
                TextView swatch=text(getString(paletteNames[i])+(ThemeCatalog.palette(selection)==i?" ✓":""),12,WHITE,true);
                swatches[i]=swatch;swatch.setGravity(Gravity.CENTER);
                swatch.setBackground(solid(ThemeCatalog.background(theme),14));
                LinearLayout.LayoutParams colorParams=new LinearLayout.LayoutParams(0,-1,1);
                colorParams.rightMargin=dp(5);
                colors.addView(swatch,colorParams);
                swatch.setOnClickListener(v->{
                    selection=ThemeCatalog.withPalette(selection,palette);ThemeCatalog.save(this,selection);
                    for(int k=0;k<swatches.length;k++)if(swatches[k]!=null)swatches[k].setText(getString(paletteNames[k])+(k==palette?" ✓":""));
                    selectionLabel.setContentDescription("selected_mascot_"+selection);
                    BatteryWidgetProvider.applyToAll(this);updatePreview();
                });
            }
        }
        TextView instruction=text(getString(R.string.preview_levels),13,MUTED,false);
        section.addView(instruction,lp(-1,-2,6));
        LinearLayout switches=row();levelControls=switches;
        section.addView(switches,lp(-1,48,8));
        level(switches,getString(R.string.preview_real),-1);
        level(switches,"15%",15);level(switches,"40%",40);
        level(switches,"70%",70);level(switches,"100%",100);
        level(switches,"⚡",-2);
        TextView tip=text(getString(R.string.preview_hint),11,0xFFD8D4FF,false);
        section.addView(tip,lp(-1,-2,8));
        TextView apply=text("✓  "+getString(R.string.button_apply),14,WHITE,true);
        apply.setGravity(Gravity.CENTER);
        apply.setBackground(gradient(new int[]{0xFF26BFF6,0xFFB654E3},14,0xFF95D7FF));
        apply.setOnClickListener(v->applyExisting());
        section.addView(apply,lp(-1,46,14));
    }
    private void level(LinearLayout items,String caption,int percent){
        TextView chip=text(caption,11,previewValue==percent?WHITE:0xFFE1D8FF,true);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(previewValue==percent?gradient(new int[]{0xFF06B6EF,0xFF9165EF},13,0xFFA2E6FF):solid(0xFF352A73,13));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);
        p.rightMargin=dp(3);
        items.addView(chip,p);
        chip.setSelected(previewValue==percent);chip.setFocusable(true);chip.setContentDescription("battery_preview_"+percent);chip.setTag(percent);chip.setOnClickListener(v->{previewValue=percent;refreshLevels();updatePreview();});
    }
    private void refreshLevels(){if(levelControls==null)return;for(int i=0;i<levelControls.getChildCount();i++){TextView chip=(TextView)levelControls.getChildAt(i);boolean active=((Integer)chip.getTag())==previewValue;chip.setSelected(active);chip.setTextColor(active?WHITE:0xFFE1D8FF);chip.setBackground(active?gradient(new int[]{0xFF06B6EF,0xFF9165EF},13,0xFFA2E6FF):solid(0xFF352A73,13));}}
    private void drawFooter(){
        LinearLayout card=col();
        card.setPadding(dp(14),dp(15),dp(14),dp(15));
        card.setBackground(gradient(new int[]{0xFF30237B,0xFF283D8B},18,0xFF6B60A9));
        root.addView(card,lp(-1,-2,18));
        card.addView(text("🔒  "+getString(R.string.privacy_title),15,WHITE,true));
        card.addView(text(getString(R.string.privacy_info),12,MUTED,false),lp(-1,-2,6));
        TextView privacy=action(getString(R.string.privacy_read),false,0);
        privacy.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle(R.string.privacy_full_title)
            .setMessage(R.string.privacy_full_body)
            .setPositiveButton(android.R.string.ok,null)
            .show());
        card.addView(privacy,lp(-1,50,12));
        TextView share=text("♡  "+getString(R.string.share_design)+"   ↗",15,0xFFFFC4EB,true);
        share.setGravity(Gravity.CENTER);
        root.addView(share,lp(-1,52,8));
        share.setOnClickListener(v->{
            Intent out=new Intent(Intent.ACTION_SEND);out.setType("text/plain");
            out.putExtra(Intent.EXTRA_TEXT,getString(R.string.share_message));
            startActivity(Intent.createChooser(out,getString(R.string.share)));
        });
    }
    private void updatePreview(){
        if(heroArt==null)return;
        BatteryState actual=BatteryState.read(this);
        int pct=previewValue==-1?actual.percent:previewValue==-2?85:previewValue;
        boolean charging=previewValue==-2||(previewValue==-1&&actual.charging);
        final int theme=ThemeCatalog.resolve(this,selection,pct,charging);
        final String key=theme+":"+pct+":"+charging;
        if(!key.equals(requestedPreview)){
            requestedPreview=key;
            final int generation=++previewGeneration;
            previewWorker.getQueue().clear();
            android.graphics.Bitmap cached=previews.get(key);
            if(cached!=null)heroArt.setImageBitmap(cached);
            else previewWorker.execute(()->{
                if(Thread.currentThread().isInterrupted()||generation!=previewGeneration)return;
                android.graphics.Bitmap bmp=BatteryArt.draw(getApplicationContext(),540,335,theme,pct,charging);
                previews.put(key,bmp);
                uiHandler.post(()->{if(!isFinishing()&&!isDestroyed()&&generation==previewGeneration&&heroArt!=null)heroArt.setImageBitmap(bmp);});
            });
        }
        String label=previewValue==-1?getString(R.string.battery_real_label)
             :previewValue==-2?getString(R.string.charging_label)
             :getString(R.string.preview_level);
        heroStatus.setText(label+"   •   "+(pct<0?"--%":pct+"%")+(previewValue==-1&&charging?" · "+getString(R.string.charging):""));
    }
    private int widgetCount(){
        return AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this,BatteryWidgetProvider.class)).length;
    }
    private void updateWidgetAction(){
        int count=widgetCount();
        if(count>0)getSharedPreferences("widget_pin",0).edit().remove("requested_at").apply();
        if(widgetAction!=null)widgetAction.setText(getString(count>0?R.string.widget_update_existing:R.string.button_add)+"    ›");
        if(widgetCountNotice!=null){
            widgetCountNotice.setVisibility(count>1?View.VISIBLE:View.GONE);
            widgetCountNotice.setText(getString(R.string.widget_duplicate_notice));
        }
    }
    private void applyExisting(){pin();}
    private void pin(){
        AppWidgetManager am=AppWidgetManager.getInstance(this);
        ComponentName provider=new ComponentName(this,BatteryWidgetProvider.class);
        if(widgetCount()>0){
            BatteryWidgetProvider.applyToAll(this);
            updateWidgetAction();
            Toast.makeText(this,widgetCount()>1?R.string.widget_duplicate_notice:R.string.text_apply_success,Toast.LENGTH_LONG).show();
            return;
        }
        android.content.SharedPreferences prefs=getSharedPreferences("widget_pin",0);
        long now=System.currentTimeMillis(),previous=prefs.getLong("requested_at",0);
        if(previous>0&&now>=previous&&now-previous<PIN_COOLDOWN_MS){
            Toast.makeText(this,R.string.widget_pin_pending,Toast.LENGTH_LONG).show();return;
        }
        if(am.isRequestPinAppWidgetSupported()){
            prefs.edit().putLong("requested_at",now).apply();
            try{
                if(am.requestPinAppWidget(provider,null,null)){
                    Toast.makeText(this,R.string.pin_requested,Toast.LENGTH_LONG).show();return;
                }
            }catch(RuntimeException ignored){}
            prefs.edit().remove("requested_at").apply();
        }
        Toast.makeText(this,R.string.manual_pin,Toast.LENGTH_LONG).show();
    }
}
