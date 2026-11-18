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
  private final int ink=0xFF2D2845,muted=0xFF736F87,accent=0xFF755BE5,bg=0xFFF7F6FD;
  private ImageView preview;private TextView status;
  @Override public void onCreate(Bundle b){super.onCreate(b);build();}
  @Override public void onResume(){super.onResume();refresh();BatteryWidgetProvider.refresh(this);}
  private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable shape(int c,int radius){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(radius));return d;}
  private TextView text(String value,int sp,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
  private LinearLayout.LayoutParams params(int height,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,height<0?height:dp(height));p.topMargin=dp(top);return p;}
  private void build(){
    ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(bg);
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(38),dp(22),dp(38));sc.addView(root);setContentView(sc);
    if(Build.VERSION.SDK_INT>=35)sc.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars());root.setPadding(dp(22),dp(22)+i.top,dp(22),dp(38)+i.bottom);return insets;});
    root.addView(text("✦  LAURAAPPS  /  EMOJI BATTERY",12,accent,true),params(-2,0));
    root.addView(text(getString(R.string.headline),31,ink,true),params(-2,24));
    root.addView(text(getString(R.string.subtitle),15,muted,false),params(-2,10));
    LinearLayout card=new LinearLayout(this);card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setOrientation(LinearLayout.VERTICAL);card.setBackground(shape(Color.WHITE,24));root.addView(card,params(-2,26));
    card.addView(text(getString(R.string.live_preview),12,muted,true),params(-2,0));
    preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.FIT_XY);card.addView(preview,params(188,13));
    status=text("",13,muted,false);card.addView(status,params(-2,12));
    root.addView(text(getString(R.string.choose_theme),23,ink,true),params(-2,28));
    int[] labels={R.string.theme_pink,R.string.theme_lavender,R.string.theme_mint,R.string.theme_dark};
    for(int row=0;row<2;row++){
      LinearLayout band=new LinearLayout(this);band.setOrientation(LinearLayout.HORIZONTAL);root.addView(band,params(92,14));
      for(int col=0;col<2;col++){
        final int index=row*2+col;
        LinearLayout tile=new LinearLayout(this);tile.setPadding(dp(15),dp(9),dp(8),dp(8));tile.setOrientation(LinearLayout.VERTICAL);tile.setBackground(shape(ThemeCatalog.BASE[index],18));
        LinearLayout.LayoutParams tileParams=new LinearLayout.LayoutParams(0,-1,1);if(col==1)tileParams.leftMargin=dp(10);band.addView(tile,tileParams);
        tile.addView(text(ThemeCatalog.FACES[index][1]+(ThemeCatalog.load(this)==index?"  ✓":""),28,ThemeCatalog.INK[index],true));
        tile.addView(text(getString(labels[index]),14,ThemeCatalog.INK[index],true));
        tile.setOnClickListener(v->{ThemeCatalog.save(this,index);BatteryWidgetProvider.refresh(this);build();Toast.makeText(this,R.string.style_saved,Toast.LENGTH_SHORT).show();});
      }
    }
    Button add=new Button(this);add.setText(getString(R.string.add_widget));add.setTextColor(Color.WHITE);add.setAllCaps(false);add.setTextSize(16);add.setBackground(shape(accent,18));add.setOnClickListener(v->pin());root.addView(add,params(58,23));
    TextView help=text(getString(R.string.widget_help),13,muted,false);help.setGravity(Gravity.CENTER);root.addView(help,params(-2,14));
    LinearLayout privacy=new LinearLayout(this);privacy.setPadding(dp(18),dp(18),dp(18),dp(18));privacy.setOrientation(LinearLayout.VERTICAL);privacy.setBackground(shape(0xFFECEAF8,20));root.addView(privacy,params(-2,26));
    privacy.addView(text(getString(R.string.privacy_title),17,ink,true));privacy.addView(text(getString(R.string.privacy_info),13,muted,false),params(-2,10));
    TextView share=text(getString(R.string.share)+" ↗",15,accent,true);share.setGravity(Gravity.CENTER);root.addView(share,params(48,13));
    share.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,getString(R.string.share_message));startActivity(Intent.createChooser(i,getString(R.string.share)));});
    refresh();
  }
  private void refresh(){if(preview==null)return;BatteryState s=BatteryState.read(this);preview.setImageBitmap(BatteryArt.draw(this,720,380,ThemeCatalog.load(this),s.percent,s.charging));status.setText(s.percent<0?getString(R.string.unknown):s.percent+"%  •  "+getString(s.charging?R.string.charging:R.string.battery_level));}
  private void pin(){AppWidgetManager m=AppWidgetManager.getInstance(this);if(Build.VERSION.SDK_INT>=26&&m.isRequestPinAppWidgetSupported()){if(m.requestPinAppWidget(new ComponentName(this,BatteryWidgetProvider.class),null,null)){Toast.makeText(this,R.string.pin_requested,Toast.LENGTH_LONG).show();return;}}Toast.makeText(this,R.string.manual_pin,Toast.LENGTH_LONG).show();}
}
