package com.lauraapps.emojibattery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

public final class BatteryArt {
    private BatteryArt(){}
    private static Paint p(int c){Paint x=new Paint(3);x.setColor(c);return x;}
    private static Paint stroke(int c,float width){Paint x=p(c);x.setStyle(Paint.Style.STROKE);x.setStrokeWidth(width);x.setStrokeCap(Paint.Cap.ROUND);x.setStrokeJoin(Paint.Join.ROUND);return x;}
    private static int a(int c,int alpha){return (c & 0xffffff) | (alpha<<24);}
    private static void oval(Canvas c,float l,float t,float r,float b,int color){c.drawOval(new RectF(l,t,r,b),p(color));}
    private static void rr(Canvas c,float l,float t,float r,float b,float rad,int col){c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p(col));}
    private static void star(Canvas c,float x,float y,float rad,int color){
        Path z=new Path();z.moveTo(x,y-rad);z.quadTo(x+rad*.15f,y-rad*.15f,x+rad,y);
        z.quadTo(x+rad*.15f,y+rad*.15f,x,y+rad);z.quadTo(x-rad*.15f,y+rad*.15f,x-rad,y);
        z.quadTo(x-rad*.15f,y-rad*.15f,x,y-rad);z.close();c.drawPath(z,p(color));
    }
    public static Bitmap draw(Context context,int w,int h,int theme,int level,boolean charging){
        theme=ThemeCatalog.clamp(theme);
        w=Math.max(200,Math.min(750,w));h=Math.max(125,Math.min(550,h));
        Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bitmap);
        c.drawColor(Color.TRANSPARENT);
        c.save();
        c.scale(w/420f,h/260f);
        paint(c,theme,level,charging);
        c.restore();
        return bitmap;
    }
    private static void paint(Canvas c,int t,int level,boolean charging){
        int accent=ThemeCatalog.ACCENT[t], ink=ThemeCatalog.INK[t];
        int back=ThemeCatalog.BASE[t], body=ThemeCatalog.BODY[t];
        Paint bg=p(back);
        bg.setShader(new LinearGradient(0,0,420,260,new int[]{back,0xFFFFFFFF,back},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(0,0,420,260),29,29,bg);
        oval(c,305,-60,480,115,a(accent,38));
        oval(c,-65,158,85,322,a(accent,34));
        star(c,39,45,10,0xFFFFCB4E);star(c,344,33,8,0xFFFFFFFF);
        star(c,381,98,11,0xFFFFB93F);star(c,20,189,6,a(accent,150));
        star(c,305,55,5,accent);star(c,79,20,5,0xFFFFFFFF);
        // Battery shadows and raised glass shell.
        rr(c,111,121,402,241,33,a(ink,30));
        rr(c,109,111,398,232,33,0xFFFFFFFF);
        rr(c,119,121,388,222,26,ink);
        rr(c,125,127,382,216,22,0xFFFFF7FF);
        rr(c,385,151,412,189,8,ink);
        rr(c,388,154,407,185,5,0xFFFFD26C);
        int pct=Math.max(0,Math.min(100,level<0?0:level));
        float end=132f+244f*pct/100f;
        if(pct>0){
          Paint fill=p(accent);
          fill.setShader(new LinearGradient(126,133,379,207,new int[]{0xFFFFC753,accent},null,Shader.TileMode.CLAMP));
          rr(c,132,133,Math.max(144,end),210,18,0xFFFFC753);
          c.drawRoundRect(new RectF(132,133,Math.max(144,end),210),18,18,fill);
        }
        rr(c,140,133,371,145,7,a(Color.WHITE,110));
        // Lightning in foreground, deliberately drawn from vectors, not copied emoji.
        Path bolt=new Path();
        bolt.moveTo(155,147);bolt.lineTo(172,147);bolt.lineTo(163,165);
        bolt.lineTo(176,165);bolt.lineTo(150,198);bolt.lineTo(155,175);
        bolt.lineTo(144,175);bolt.close();
        c.drawPath(bolt,p(0xFFFFDE62));c.drawPath(bolt,stroke(0xFFFF8B28,3));
        Paint number=p(Color.WHITE);
        number.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
        number.setTextSize(level==100?69:74);
        number.setTextAlign(Paint.Align.CENTER);
        String value=level<0?"--%":level+"%";
        c.drawText(value,284,197,stroke(a(ink,95),7));
        c.drawText(value,284,197,number);
        // Original mascot overlaps battery: body and expressive paws.
        drawCreature(c,t,level,charging,body,ink);
        // Charging sparks are visible only while connected to power.
        if(charging){star(c,356,79,14,0xFFFFBC30);star(c,381,57,7,0xFFFE8A4D);}
    }
    private static void ear(Canvas c,int kind,int body,int secondary,int ink) {
        if(kind==0){ // original axolotl coral frills
          for(int side:new int[]{-1,1})for(int i=0;i<3;i++){
            float ex=side*(49+i*4),ey=-35+i*15;
            oval(c,ex-12,ey-15,ex+12,ey+15,secondary);
            c.drawOval(new RectF(ex-12,ey-15,ex+12,ey+15),stroke(a(ink,85),2));
          }
        }else if(kind==1){ // tiny dragon horns and wings
          Path horn=new Path();horn.moveTo(-44,-35);horn.lineTo(-43,-86);horn.lineTo(-19,-60);horn.close();
          c.drawPath(horn,p(0xFFFFC666));
          Path horn2=new Path();horn2.moveTo(22,-63);horn2.lineTo(45,-88);horn2.lineTo(47,-30);horn2.close();c.drawPath(horn2,p(0xFFFFC666));
          oval(c,-72,-45,-43,8,secondary);oval(c,42,-45,72,8,secondary);
        }else if(kind==2){ // frog eyes
          oval(c,-58,-67,-8,-15,body);oval(c,8,-67,58,-15,body);
        }else if(kind==3){ // floppy dog ears
          oval(c,-69,-52,-37,35,secondary);oval(c,38,-52,69,35,secondary);
        }else if(kind==4){ // penguin head crest
          Path tuft=new Path();tuft.moveTo(-10,-49);tuft.quadTo(-35,-95,4,-70);tuft.quadTo(29,-87,13,-43);tuft.close();c.drawPath(tuft,p(body));
        }else { // glowing firefly antennae
          c.drawLine(-26,-43,-37,-77,stroke(ink,6));c.drawLine(26,-43,37,-77,stroke(ink,6));
          oval(c,-49,-94,-26,-72,0xFFFFDF69);oval(c,26,-94,49,-72,0xFFFFDF69);
          oval(c,-70,8,-38,53,secondary);oval(c,38,8,70,53,secondary);
        }
    }
    private static void drawCreature(Canvas c,int kind,int level,boolean charging,int body,int ink){
        c.save();c.translate(108,102);
        // wings and ears are character-specific, designed for LauraApps.
        ear(c,kind,body,ThemeCatalog.SECONDARY[kind],ink);
        oval(c,-53,-47,53,57,ink);
        oval(c,-50,-45,50,53,body);
        if(kind==4){oval(c,-37,-24,38,51,0xFFF4FAFF);oval(c,-48,0,-25,37,0xFFF4FAFF);oval(c,24,0,48,37,0xFFF4FAFF);}
        if(kind==3)oval(c,-27,-3,26,46,0xFFFFF8EF);
        if(kind==0)oval(c,-29,13,30,47,0xFFFFF4DD);
        if(kind==2)oval(c,-28,14,29,51,0xFFD9FFC1);
        // eye expression computed from actual battery, never a fixed percentage image
        boolean low=level>=0&&level<=20&&!charging;
        boolean tired=level>20&&level<=45&&!charging;
        boolean delighted=level>=85||charging;
        for(int side:new int[]{-1,1}){
          float ex=side*22;
          if(tired){
             c.drawArc(new RectF(ex-12,-15,ex+12,3),185,170,false,stroke(ink,4));
          }else if(delighted){
             c.drawArc(new RectF(ex-12,-12,ex+12,13),188,167,false,stroke(ink,5));
          }else{
             oval(c,ex-12,-14,ex+12,13,ink);
             oval(c,ex-5,-10,ex+2,1,Color.WHITE);
             oval(c,ex+4,2,ex+7,6,a(Color.WHITE,180));
          }
          oval(c,ex-17,12,ex-1,24,0xFFFF9BB5);
        }
        if(low){
          c.drawArc(new RectF(-10,22,12,41),200,138,false,stroke(ink,3.5f));
          oval(c,34,-39,45,-18,0xFF6CD4FF);
          c.drawLine(-28,-22,-15,-26,stroke(ink,3));
          c.drawLine(15,-26,28,-22,stroke(ink,3));
        }else if(tired){
          oval(c,-5,24,5,31,ink);
          star(c,43,-32,5,0xFFFFD666);
        }else{
          c.drawArc(new RectF(-14,14,14,43),2,177,false,stroke(ink,3.5f));
          if(delighted){oval(c,-7,25,7,39,0xFFFF6688);}
        }
        // paws on top of the battery (soft curved hands)
        oval(c,-51,41,-13,65,ink);oval(c,-49,42,-16,62,0xFFFFF8F7);
        oval(c,15,41,53,65,ink);oval(c,18,42,51,62,0xFFFFF8F7);
        c.restore();
    }
}