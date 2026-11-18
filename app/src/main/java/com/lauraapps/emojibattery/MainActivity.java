package com.lauraapps.emojibattery;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
  private final int ink=0xFF26213F, muted=0xFF76718D, purple=0xFF7B54E9, background=0xFFF9F6FF;
  private ImageView preview;
  private TextView statusLabel;
  private int previewPercent=-1; // -1 = real, -2 = charging demo
  private final int[] names={R.string.theme_pink,R.string.theme_lavender,R.string.theme_mint,R.string.theme_dark,R.string.theme_sun,R.string.theme_star,R.string.theme_peach2,R.string.theme_lavender2,R.string.theme_mint2,R.string.theme_dog2,R.string.theme_penguin2,R.string.theme_firefly2};
  @Override public void onCreate(Bundle state){super.onCreate(state);build();}
  @Override protected void onResume(){super.onResume();refresh();BatteryWidgetProvider.refresh(this);}
  private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  private GradientDrawable bg(int color,int r){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(r));return d;}
  private GradientDrawable gradient(int a,int b,int r){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(r));return d;}
  private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));return t;}
  private LinearLayout.LayoutParams lp(int w,int h,int top){LinearLayout.LayoutParams v=new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));v.topMargin=dp(top);return v;}
  private void build(){
    getWindow().setStatusBarColor(background);getWindow().setNavigationBarColor(background);
    ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(background);
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(19),dp(28),dp(19),dp(35));sc.addView(root);
    setContentView(sc);
    if(Build.VERSION.SDK_INT>=35)sc.setOnApplyWindowInsetsListener((v,i)->{
      android.graphics.Insets z=i.getInsets(WindowInsets.Type.systemBars());root.setPadding(dp(19),dp(22)+z.top,dp(19),dp(35)+z.bottom);return i;});
    root.addView(text("✦  LAURAAPPS   /   DIY STUDIO",12,purple,true));
    root.addView(text(getString(R.string.headline),29,ink,true),lp(-1,-2,17));
    root.addView(text(getString(R.string.subtitle),14,muted,false),lp(-1,-2,7));
    LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(16),dp(16),dp(16),dp(16));hero.setBackground(gradient(0xFFEEE5FF,0xFFFFFFFF,25));root.addView(hero,lp(-1,-2,22));
    hero.addView(text(getString(R.string.live_preview),12,purple,true));
    preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.FIT_XY);hero.addView(preview,lp(-1,224,10));
    statusLabel=text("",13,muted,false);statusLabel.setGravity(Gravity.CENTER);hero.addView(statusLabel,lp(-1,-2,8));
    root.addView(text(getString(R.string.preview_levels),19,ink,true),lp(-1,-2,21));
    LinearLayout levels=new LinearLayout(this);levels.setOrientation(LinearLayout.HORIZONTAL);root.addView(levels,lp(-1,44,10));
    addLevel(levels,getString(R.string.preview_real),-1);
    addLevel(levels,"15%",15);addLevel(levels,"40%",40);addLevel(levels,"70%",70);addLevel(levels,"100%",100);
    addLevel(levels,"⚡",-2);
    TextView hint=text(getString(R.string.preview_hint),12,muted,false);root.addView(hint,lp(-1,-2,10));
    root.addView(text(getString(R.string.choose_theme),23,ink,true),lp(-1,-2,26));
    TextView count=text("12  •  DIY COLLECTION",12,purple,true);root.addView(count,lp(-1,-2,5));
    BatteryState state=BatteryState.read(this);
    for(int row=0;row<ThemeCatalog.COUNT/2;row++){
      LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);root.addView(line,lp(-1,157,12));
      for(int col=0;col<2;col++){
        final int index=row*2+col;
        LinearLayout cell=new LinearLayout(this);cell.setOrientation(LinearLayout.VERTICAL);cell.setPadding(dp(7),dp(7),dp(7),dp(9));cell.setBackground(bg(0xFFFFFFFF,18));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);if(col==1)cp.leftMargin=dp(9);line.addView(cell,cp);
        ImageView mini=new ImageView(this);mini.setScaleType(ImageView.ScaleType.FIT_XY);
        mini.setImageBitmap(BatteryArt.draw(this,370,220,index,state.percent,state.charging));cell.addView(mini,lp(-1,106,0));
        TextView name=text(getString(names[index])+(ThemeCatalog.load(this)==index?"  ✓":""),12,ink,true);
        name.setGravity(Gravity.CENTER);name.setSingleLine(true);name.setTextSize(11);cell.addView(name,lp(-1,-2,7));
        cell.setOnClickListener(v->{ThemeCatalog.save(this,index);BatteryWidgetProvider.refresh(this);build();Toast.makeText(this,R.string.style_saved,Toast.LENGTH_SHORT).show();});
      }
    }
    Button add=new Button(this);add.setText(getString(R.string.add_widget)+"  +");add.setTextSize(16);add.setAllCaps(false);add.setTextColor(Color.WHITE);add.setBackground(gradient(0xFF8662FF,0xFFFF6CB0,18));add.setOnClickListener(v->pin());root.addView(add,lp(-1,62,24));
    TextView help=text(getString(R.string.widget_help),13,muted,false);help.setGravity(Gravity.CENTER);root.addView(help,lp(-1,-2,13));
    LinearLayout privacy=new LinearLayout(this);privacy.setPadding(dp(16),dp(16),dp(16),dp(16));privacy.setOrientation(LinearLayout.VERTICAL);privacy.setBackground(bg(0xFFF1EDFC,20));root.addView(privacy,lp(-1,-2,25));
    privacy.addView(text(getString(R.string.privacy_title),16,ink,true));
    privacy.addView(text(getString(R.string.privacy_info),13,muted,false),lp(-1,-2,7));
    TextView share=text(getString(R.string.share)+"  ↗",15,purple,true);share.setGravity(Gravity.CENTER);root.addView(share,lp(-1,52,12));
    share.setOnClickListener(v->{Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,getString(R.string.share_message));startActivity(Intent.createChooser(send,getString(R.string.share)));});
    refresh();
  }
  private void addLevel(LinearLayout parent,String label,int value){
    TextView chip=text(label,12,previewPercent==value?Color.WHITE:purple,true);
    chip.setGravity(Gravity.CENTER);chip.setBackground(bg(previewPercent==value?purple:0xFFECE5FF,12));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1);p.rightMargin=dp(4);parent.addView(chip,p);
    chip.setOnClickListener(v->{previewPercent=value;build();});
  }
  private void refresh(){
    if(preview==null)return;
    BatteryState real=BatteryState.read(this);
    int pct=previewPercent==-1||previewPercent==-2&&real.percent<0?real.percent:previewPercent==-2?85:previewPercent;
    boolean charging=previewPercent==-2||previewPercent==-1&&real.charging;
    preview.setImageBitmap(BatteryArt.draw(this,720,440,ThemeCatalog.load(this),pct,charging));
    String caption=previewPercent==-1?getString(R.string.preview_real)+"  •  "+(real.percent<0?"--%":real.percent+"%"):
      previewPercent==-2?getString(R.string.preview_charging)+"  •  85%":getString(R.string.preview_percentage,previewPercent);
    statusLabel.setText(caption);
  }
  private void pin(){
    AppWidgetManager am=AppWidgetManager.getInstance(this);
    if(Build.VERSION.SDK_INT>=26&&am.isRequestPinAppWidgetSupported()){
      if(am.requestPinAppWidget(new ComponentName(this,BatteryWidgetProvider.class),null,null)){
        Toast.makeText(this,R.string.pin_requested,Toast.LENGTH_LONG).show();return;
      }
    }
    Toast.makeText(this,R.string.manual_pin,Toast.LENGTH_LONG).show();
  }
}