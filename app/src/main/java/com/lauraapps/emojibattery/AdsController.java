package com.lauraapps.emojibattery;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.*;
import com.google.android.gms.ads.nativead.*;
import com.google.android.ump.*;

/** Ads are restricted to visible Activity content; never used by widgets or dreams. */
final class AdsController {
    private final Activity activity;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final CategoryAdGate gate;
    private final Runnable refreshUi;
    private boolean foreground, destroyed, initialized, initializing, consentReady, showing;
    private boolean bannerLoading,bannerLoaded,nativeLoading,interstitialLoading;
    private long bannerRetry,nativeRetry,interstitialRetry,nativeAt,interstitialAt;
    private int bannerWidth;
    private AdView banner;
    private NativeAd nativeAd;
    private InterstitialAd interstitial;
    private FrameLayout bannerHost,nativeHost;
    private Runnable pendingNavigation;
    AdsController(Activity a,Runnable refreshUi,int count,long last){
        activity=a;this.refreshUi=refreshUi;gate=new CategoryAdGate(count,last);
    }
    int count(){return gate.count();}
    long last(){return gate.last();}
    private boolean alive(){return !destroyed&&!activity.isFinishing()&&!activity.isDestroyed();}
    private boolean allowed(){return alive()&&consentReady&&UserMessagingPlatform.getConsentInformation(activity).canRequestAds();}
    private boolean usable(){return allowed()&&initialized&&foreground&&!showing;}
    private long now(){return SystemClock.elapsedRealtime();}
    private int dp(int v){return Math.round(v*activity.getResources().getDisplayMetrics().density);}
    private void log(String s){android.util.Log.d("EmojiAds",s);}
    void begin(){
        if(BuildConfig.DEBUG && activity.getIntent().getBooleanExtra("qa_skip_ads",false))return;
        UserMessagingPlatform.getConsentInformation(activity).requestConsentInfoUpdate(activity,new ConsentRequestParameters.Builder().build(),
          ()->{if(alive())UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,e->consentComplete());},
          e->{log("consent update failed "+e.getErrorCode()+" "+e.getMessage());consentComplete();});
    }
    private void consentComplete(){
        if(!alive())return;
        consentReady=true;
        if(!allowed()){log("ads unavailable: consent cannot request ads");clearAds();return;}
        if(!initialized&&!initializing){
            initializing=true;
            MobileAds.initialize(activity,status->handler.post(()->{
                if(!alive())return;initializing=false;initialized=true;loadVisible();
            }));
        }else loadVisible();
    }
    void privacy(){
        if(!alive())return;
        UserMessagingPlatform.showPrivacyOptionsForm(activity,error->{
            if(!alive())return;
            clearAds();consentComplete();
            if(error!=null)Toast.makeText(activity,R.string.ads_privacy_unavailable,Toast.LENGTH_SHORT).show();
        });
    }
    void resume(){foreground=true;if(banner!=null)banner.resume();loadVisible();}
    void pause(){foreground=false;if(banner!=null)banner.pause();}
    void destroy(){destroyed=true;handler.removeCallbacksAndMessages(null);pendingNavigation=null;clearAds();}
    private void detach(View v){if(v!=null&&v.getParent() instanceof ViewGroup)((ViewGroup)v.getParent()).removeView(v);}
    private void clearAds(){
        if(banner!=null){detach(banner);banner.destroy();banner=null;}
        if(nativeAd!=null){nativeAd.destroy();nativeAd=null;}
        interstitial=null;bannerLoaded=false;bannerLoading=false;
        if(bannerHost!=null){bannerHost.removeAllViews();bannerHost.setVisibility(View.GONE);}
        if(nativeHost!=null){nativeHost.removeAllViews();nativeHost.setVisibility(View.GONE);}
    }
    void hosts(FrameLayout b,FrameLayout n){
        if(bannerHost!=b&&banner!=null)detach(banner);
        if(nativeHost!=n&&nativeHost!=null){nativeHost.removeAllViews();nativeHost.setVisibility(View.GONE);}
        bannerHost=b;nativeHost=n;loadVisible();
    }
    private void loadVisible(){
        if(!usable())return;
        if(bannerHost!=null)loadBanner();
        if(nativeHost!=null)loadNative();
        loadInterstitial();
    }
    private void loadBanner(){
        FrameLayout host=bannerHost;
        if(host==null)return;
        host.post(()->{
            if(host!=bannerHost||!usable()||!host.isAttachedToWindow())return;
            int px=host.getWidth();
            if(px<=0&&host.getParent() instanceof View)px=((View)host.getParent()).getWidth();
            if(px<=0)return;
            int width=(int)(px/activity.getResources().getDisplayMetrics().density);
            if(width<=0)return;
            if(banner!=null&&bannerWidth!=width){detach(banner);banner.destroy();banner=null;bannerLoaded=false;bannerLoading=false;}
            if(banner==null){
                bannerWidth=width;banner=new AdView(activity);
                final AdView candidate=banner;
                candidate.setAdUnitId(BuildConfig.BANNER_ID);
                candidate.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity,width));
                candidate.setAdListener(new AdListener(){
                    @Override public void onAdLoaded(){if(candidate!=banner||!alive())return;bannerLoading=false;bannerLoaded=true;attachBanner();log("banner loaded");}
                    @Override public void onAdImpression(){log("banner impression");}
                    @Override public void onAdFailedToLoad(LoadAdError error){
                        if(candidate!=banner||!alive())return;bannerLoading=false;bannerRetry=now()+60000;
                        if(!bannerLoaded){if(bannerHost!=null)bannerHost.setVisibility(View.GONE);handler.postDelayed(()->{if(usable()&&bannerHost!=null)loadBanner();},60000);}
                        log("banner load failed "+error.getCode());
                    }
                });
            }
            if(bannerLoaded){attachBanner();return;}
            if(bannerLoading||now()<bannerRetry)return;
            bannerLoading=true;log("banner request");banner.loadAd(new AdRequest.Builder().build());
        });
    }
    private void attachBanner(){
        if(!usable()||bannerHost==null||banner==null||!bannerLoaded)return;
        if(banner.getParent()!=bannerHost){detach(banner);bannerHost.removeAllViews();FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.CENTER);bannerHost.addView(banner,p);}
        bannerHost.setVisibility(View.VISIBLE);
    }
    private void loadNative(){
        if(!usable()||nativeHost==null)return;
        if(nativeAd!=null&&now()-nativeAt>=3600000){nativeHost.removeAllViews();nativeAd.destroy();nativeAd=null;}
        if(nativeAd!=null){attachNative();return;}
        if(nativeLoading||now()<nativeRetry)return;
        nativeLoading=true;log("native request");
        new AdLoader.Builder(activity,BuildConfig.NATIVE_ID)
          .forNativeAd(ad->{
              nativeLoading=false;
              if(!allowed()){ad.destroy();return;}
              if(nativeAd!=null)nativeAd.destroy();
              nativeAd=ad;nativeAt=now();nativeRetry=0;log("native loaded");handler.post(()->attachNative());
          })
          .withAdListener(new AdListener(){
              @Override public void onAdImpression(){log("native impression");}
              @Override public void onAdFailedToLoad(LoadAdError e){nativeLoading=false;nativeRetry=now()+60000;log("native load failed "+e.getCode()+" "+e.getMessage());handler.postDelayed(()->{if(usable()&&nativeHost!=null)loadNative();},60000);}
          }).build().loadAd(new AdRequest.Builder().build());
    }
    private TextView text(String value,int sp,int color){
        TextView t=new TextView(activity);t.setText(value);t.setTextSize(sp);t.setTextColor(color);return t;
    }
    private void attachNative(){
        if(!usable()||nativeHost==null||nativeAd==null)return;
        final FrameLayout host=nativeHost;
        if(!host.isAttachedToWindow()){host.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
            @Override public void onViewAttachedToWindow(View v){v.removeOnAttachStateChangeListener(this);if(host==nativeHost)host.post(()->attachNative());}
            @Override public void onViewDetachedFromWindow(View v){}
        });return;}
        if(nativeHost.getChildCount()>0){nativeHost.setVisibility(View.VISIBLE);return;}
        NativeAdView view=new NativeAdView(activity);
        LinearLayout box=new LinearLayout(activity);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),dp(10),dp(12),dp(12));box.setBackgroundColor(0xffe9e5ff);
        view.addView(box,new FrameLayout.LayoutParams(-1,-2));
        TextView attribution=text(activity.getString(R.string.ads_label),12,0xff443b68);attribution.setPadding(0,0,dp(32),dp(6));box.addView(attribution);
        AdChoicesView choices=new AdChoicesView(activity);
        FrameLayout.LayoutParams choiceParams=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.TOP|android.view.Gravity.END);view.addView(choices,choiceParams);view.setAdChoicesView(choices);
        TextView headline=text(nativeAd.getHeadline(),17,0xff201b40);headline.setTypeface(null,Typeface.BOLD);box.addView(headline);view.setHeadlineView(headline);
        if(nativeAd.getMediaContent()!=null){MediaView media=new MediaView(activity);box.addView(media,new LinearLayout.LayoutParams(-1,dp(150)));media.setMediaContent(nativeAd.getMediaContent());view.setMediaView(media);}
        if(nativeAd.getIcon()!=null){ImageView icon=new ImageView(activity);icon.setImageDrawable(nativeAd.getIcon().getDrawable());box.addView(icon,new LinearLayout.LayoutParams(dp(48),dp(48)));view.setIconView(icon);}
        if(nativeAd.getAdvertiser()!=null){TextView t=text(nativeAd.getAdvertiser(),12,0xff494264);box.addView(t);view.setAdvertiserView(t);}
        if(nativeAd.getBody()!=null){TextView body=text(nativeAd.getBody(),14,0xff30294d);box.addView(body);view.setBodyView(body);}
        if(nativeAd.getCallToAction()!=null){Button cta=new Button(activity);cta.setText(nativeAd.getCallToAction());box.addView(cta);view.setCallToActionView(cta);}
        nativeHost.addView(view,new FrameLayout.LayoutParams(-1,-2));nativeHost.setVisibility(View.VISIBLE);view.setNativeAd(nativeAd);nativeHost.requestLayout();view.requestLayout();log("native attached to gallery");
    }
    void category(Runnable navigate){
        if(showing)return;
        gate.visit();
        if(interstitial!=null&&now()-interstitialAt>=3600000)interstitial=null;
        if(usable()&&gate.eligible(now())&&interstitial!=null){
            InterstitialAd ad=interstitial;interstitial=null;showing=true;pendingNavigation=navigate;
            ad.setFullScreenContentCallback(new FullScreenContentCallback(){
                @Override public void onAdShowedFullScreenContent(){gate.shown(now());log("interstitial shown");}
                @Override public void onAdImpression(){log("interstitial impression");}
                @Override public void onAdDismissedFullScreenContent(){finishInterstitial();}
                @Override public void onAdFailedToShowFullScreenContent(AdError e){log("interstitial show failed "+e.getCode());finishInterstitial();}
            });
            try{ad.show(activity);}catch(RuntimeException ex){finishInterstitial();}
        }else{navigate.run();loadInterstitial();}
    }
    private void finishInterstitial(){
        showing=false;Runnable next=pendingNavigation;pendingNavigation=null;
        if(alive()&&next!=null)next.run();
        if(alive())loadVisible();
    }
    private void loadInterstitial(){
        if(!usable()||interstitial!=null||interstitialLoading||now()<interstitialRetry)return;
        interstitialLoading=true;log("interstitial request");
        InterstitialAd.load(activity,BuildConfig.INTERSTITIAL_ID,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){
            @Override public void onAdLoaded(InterstitialAd ad){interstitialLoading=false;if(!allowed())return;interstitial=ad;interstitialAt=now();log("interstitial loaded");}
            @Override public void onAdFailedToLoad(LoadAdError error){interstitialLoading=false;interstitialRetry=now()+60000;log("interstitial load failed "+error.getCode());handler.postDelayed(()->{if(usable())loadInterstitial();},60000);}
        });
    }
}
