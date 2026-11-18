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
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class BatteryArt {
    private BatteryArt(){}
    private static volatile Bitmap atlas;
    private static boolean atlasAttempted=false;
    private static synchronized Bitmap atlas(Context c){
        if(atlasAttempted)return atlas;
        atlasAttempted=true;
        try {
            ByteArrayOutputStream buf=new ByteArrayOutputStream(18000);
            for(int part=1;part<=4;part++){
                try(InputStream in=c.getAssets().open("mascot_atlas.part"+part)){
                    byte[] chunk=new byte[4096];int n;
                    while((n=in.read(chunk))!=-1)buf.write(chunk,0,n);
                }
            }
            byte[] raw=Base64.decode(buf.toByteArray(),Base64.DEFAULT);
            atlas=BitmapFactory.decodeByteArray(raw,0,raw.length);
            if(atlas==null || atlas.getWidth()%4!=0 || atlas.getHeight()%3!=0) atlas=null;
        }catch(Exception ignored){atlas=null;}
        return atlas;
    }
    private static void illustrated(Canvas c,Bitmap sheet,int theme,int level,boolean charging){
        int kind=ThemeCatalog.species(theme);
        int col=kind%4,row=kind/4,w=sheet.getWidth()/4,h=sheet.getHeight()/3;
        int gl=charging?0xFF5BE5FD:level<=20?0xFFFF5D91:level<=45?0xFFFFB353:level<80?0xFF559FFF:0xFF55EBA2;
        Paint back=p(0xFF101448);
        back.setShader(new LinearGradient(0,0,420,260,new int[]{0xFF10163E,0xFF2F257E,0xFF732DA1},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(0,0,420,260),26,26,back);
        oval(c,-70,145,140,330,a(0xFFFF5BC0,70));
        oval(c,150,-95,490,165,a(0xFF55C4FA,60));
        star(c,36,42,10,0xFFFFCF55);star(c,339,44,7,0xFFF9A3FF);
        star(c,388,110,12,0xFFFFD96F);star(c,24,195,7,0xFF76DFFF);
        star(c,235,89,7,Color.WHITE);star(c,287,28,5,0xFFFF9AEC);
        Paint glow=p(a(gl,90));glow.setShadowLayer(18,0,0,gl);
        c.drawRoundRect(new RectF(16,13,257,250),22,22,glow);
        Paint sprite=p(Color.WHITE);
        sprite.setFilterBitmap(true);
        Rect src=new Rect(col*w,row*h,(col+1)*w,(row+1)*h);
        c.drawBitmap(sheet,src,new RectF(16,12,259,253),sprite);
        Paint label=p(0xFFE2DAFF);label.setTextSize(15);label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setTextAlign(Paint.Align.CENTER);
        c.drawText("BATTERY",337,90,label);
        int levelText=Math.max(0,Math.min(100,level));
        String value=level<0?"--%":level+"%";
        Paint number=p(Color.WHITE);number.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
        number.setTextSize(value.length()>3?57:68);number.setTextAlign(Paint.Align.CENTER);
        number.setShadowLayer(8,0,4,0xFF11123C);
        c.drawText(value,334,163,stroke(0xFF35216E,5));
        c.drawText(value,334,163,number);
        rr(c,265,189,404,205,8,0xFF4A3788);
        if(level>0)rr(c,268,192,268+Math.min(133,133*levelText/100f),202,6,gl);
        Paint state=p(0xFFFFFFFF);state.setTextAlign(Paint.Align.CENTER);
        state.setTextSize(13);
        c.drawText(charging?"⚡ CHARGING":level<0?"OFFLINE":level<=20?"LOW POWER":level>=90?"FULL OF JOY":"LIVE LEVEL",335,231,state);
    }
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
        Bitmap art=atlas(context);
        if(art!=null)illustrated(c,art,theme,level,charging);
        else paint(c,theme,level,charging);
        c.restore();
        return bitmap;
    }
    private static void paint(Canvas c,int t,int level,boolean charging){
        int accent=ThemeCatalog.ACCENT[t], ink=ThemeCatalog.INK[t];
        int back=ThemeCatalog.BASE[t], body=ThemeCatalog.BODY[t];
        Paint bg=p(0xFF14113F);
        bg.setShader(new LinearGradient(0,0,420,260,new int[]{0xFF121C54,0xFF34207B,0xFF8B42A5},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(0,0,420,260),29,29,bg);
        oval(c,305,-60,480,115,a(0xFFFB6CE7,85));
        oval(c,120,-70,370,85,a(0xFF56C6FF,48));
        oval(c,-65,158,85,322,a(0xFFFF74C2,65));
        oval(c,110,80,410,270,a(accent,32));
        star(c,39,45,10,0xFFFFCB4E);star(c,344,33,8,0xFFFFFFFF);
        star(c,381,98,11,0xFFFFB93F);star(c,20,189,6,a(accent,150));
        star(c,305,55,5,0xFFFFB5F9);
        star(c,254,84,8,0xFF6DF7FA);
        star(c,389,49,8,0xFFFF78C0);
        star(c,67,96,7,0xFFFFC96A);
        star(c,346,101,5,0xFFFFFFFF);star(c,79,20,5,0xFFFFFFFF);
        // Battery shadows and raised glass shell.
        rr(c,105,116,404,246,35,a(0xFF15EFFF,80));
        rr(c,107,111,400,239,35,0xFFAA7FFF);
        rr(c,111,115,396,235,32,0xFFF5FAFF);
        rr(c,117,121,391,228,29,0xFF23336C);
        rr(c,124,128,383,220,24,0xFF151C54);
        rr(c,387,155,411,195,9,0xFF5B39C4);
        rr(c,389,157,408,191,7,0xFFFFDB6B);
        int pct=Math.max(0,Math.min(100,level<0?0:level));
        float end=132f+244f*pct/100f;
        if(pct>0){
          Paint fill=p(accent);
          int current=pct<=20?0xFFFF4778:pct<=45?0xFFFFA441:pct<=75?0xFF2DADF9:0xFF39E879;
          int secondary=pct<=20?0xFFFF8F93:pct<=45?0xFFFFD263:pct<=75?0xFF65EDFF:0xFFAAFF78;
          fill.setShader(new LinearGradient(132,133,375,207,new int[]{secondary,current},null,Shader.TileMode.CLAMP));
          rr(c,132,133,Math.max(144,end),210,18,current);
          c.drawRoundRect(new RectF(132,133,Math.max(144,end),210),18,18,fill);
        }
        rr(c,140,133,371,141,7,a(Color.WHITE,115));
        star(c,364,137,4,Color.WHITE);
        // Lightning in foreground, deliberately drawn from vectors, not copied emoji.
        Path bolt=new Path();
        bolt.moveTo(155,147);bolt.lineTo(172,147);bolt.lineTo(163,165);
        bolt.lineTo(176,165);bolt.lineTo(150,198);bolt.lineTo(155,175);
        bolt.lineTo(144,175);bolt.close();
        c.drawPath(bolt,p(0xFFFFDE62));c.drawPath(bolt,stroke(0xFFFF8B28,3));
        Paint number=p(Color.WHITE);
        number.setShadowLayer(4,0,3,a(0xFF13143D,220));
        number.setShadowLayer(3,0,3,a(ink,180));
        number.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
        number.setTextSize(level==100?69:74);
        number.setTextAlign(Paint.Align.CENTER);
        String value=level<0?"--%":level+"%";
        c.drawText(value,284,197,stroke(a(ink,200),7));
        c.drawText(value,284,197,number);
        // Original mascot overlaps battery: body and expressive paws.
        drawCreature(c,ThemeCatalog.species(t),t,level,charging,body,ink);
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
        }else if(kind==5){ // firefly
          c.drawLine(-26,-43,-37,-77,stroke(ink,6));c.drawLine(26,-43,37,-77,stroke(ink,6));
          oval(c,-49,-94,-26,-72,0xFFFFDF69);oval(c,26,-94,49,-72,0xFFFFDF69);
          oval(c,-70,8,-38,53,secondary);oval(c,38,8,70,53,secondary);
        }else if(kind==6){ // kitten with pointy pink ears and a small bow
          Path left=new Path();left.moveTo(-50,-24);left.lineTo(-48,-82);left.quadTo(-14,-66,-17,-38);left.close();
          Path right=new Path();right.moveTo(18,-38);right.quadTo(23,-63,49,-82);right.lineTo(51,-24);right.close();
          c.drawPath(left,p(ink));c.drawPath(right,p(ink));
          c.save();c.scale(.75f,.75f);c.drawPath(left,p(secondary));c.drawPath(right,p(secondary));c.restore();
          oval(c,10,-64,27,-51,0xFFFFD45A);oval(c,26,-64,43,-51,0xFFFFD45A);
        }else if(kind==7){ // fox ears and fluffy side cheeks
          Path left=new Path();left.moveTo(-49,-27);left.lineTo(-58,-89);left.lineTo(-12,-50);left.close();
          Path right=new Path();right.moveTo(12,-50);right.lineTo(58,-89);right.lineTo(49,-27);right.close();
          c.drawPath(left,p(ink));c.drawPath(right,p(ink));
          c.drawPath(left,p(body));c.drawPath(right,p(body));
          oval(c,-60,-15,-36,28,secondary);oval(c,36,-15,60,28,secondary);
        }else if(kind==8){ // bunny with long rounded ears
          oval(c,-41,-122,-12,-32,ink);oval(c,14,-122,43,-32,ink);
          oval(c,-35,-113,-18,-35,secondary);oval(c,20,-113,37,-35,secondary);
        }else if(kind==9){ // panda with dark rounded ears
          oval(c,-60,-71,-26,-25,ink);oval(c,26,-71,60,-25,ink);
          oval(c,-53,-62,-32,-32,secondary);oval(c,32,-62,53,-32,secondary);
        }else if(kind==10){ // star unicorn with colorful horn
          Path horn=new Path();horn.moveTo(-7,-48);horn.lineTo(8,-105);horn.lineTo(24,-48);horn.close();
          c.drawPath(horn,p(0xFFFFC542));
          oval(c,-52,-67,-30,-33,secondary);oval(c,30,-67,52,-33,secondary);
          star(c,10,-86,7,Color.WHITE);
        }else if(kind==11){ // tiny astronaut, with helmet rim
          oval(c,-67,-70,67,65,ink);oval(c,-62,-65,62,59,0xFFBCEBFF);
          oval(c,-55,-58,55,52,0xFFEEF8FF);
          rr(c,-20,-78,20,-63,8,0xFFFFC955);
        }
    }
    private static void drawCreature(Canvas c,int kind,int theme,int level,boolean charging,int body,int ink){
        c.save();c.translate(108,102);
        // wings and ears are character-specific, designed for LauraApps.
        ear(c,kind,body,ThemeCatalog.SECONDARY[theme],ink);
        oval(c,-55,-49,55,60,a(ink,55));
        oval(c,-53,-47,53,57,ink);
        Paint shiny=p(body);shiny.setShader(new LinearGradient(-51,-43,40,50,new int[]{Color.WHITE,body,body},null,Shader.TileMode.CLAMP));
        c.drawOval(new RectF(-50,-45,50,53),shiny);
        oval(c,-43,-36,-12,-25,a(Color.WHITE,110));
        if(kind==9){oval(c,-43,-25,-3,20,0xFF4B456A);oval(c,4,-25,44,20,0xFF4B456A);}
        if(kind==6){oval(c,-28,16,27,41,0xFFFFF8F2);c.drawLine(-7,18,7,18,stroke(ink,2));}
        if(kind==7){oval(c,-30,18,29,46,0xFFFFFFFF);}
        if(kind==8){oval(c,-28,14,29,44,0xFFFFFAFB);}
        if(kind==10){oval(c,-25,18,26,46,0xFFFFFFFF);}
        if(kind==11){oval(c,-33,11,34,43,0xFFB1E7FF);}
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
             oval(c,ex-14,-16,ex+14,14,a(ink,40));
             oval(c,ex-12,-14,ex+12,13,ink);
             oval(c,ex-5,-10,ex+2,1,Color.WHITE);
             oval(c,ex+4,2,ex+7,6,a(Color.WHITE,180));
             oval(c,ex-10,-10,ex-7,-7,Color.WHITE);
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
        // tiny blush details and glossy paws on top of the battery
        oval(c,-40,8,-19,19,a(0xFFFF8DAE,135));oval(c,19,8,40,19,a(0xFFFF8DAE,135));
        oval(c,-51,41,-13,65,ink);oval(c,-49,42,-16,62,0xFFFFF8F7);
        oval(c,15,41,53,65,ink);oval(c,18,42,51,62,0xFFFFF8F7);
        c.restore();
    }
}