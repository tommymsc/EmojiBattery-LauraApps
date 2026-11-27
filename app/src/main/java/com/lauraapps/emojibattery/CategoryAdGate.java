package com.lauraapps.emojibattery;
/** Count intentional category visits; never interrupt more often than once per minute. */
final class CategoryAdGate {
    static final long COOLDOWN=60000L;
    private int count;
    private long last;
    CategoryAdGate(int count,long last){this.count=Math.max(0,Math.min(2,count));this.last=last;}
    void visit(){count=Math.min(2,count+1);}
    boolean eligible(long now){return count>=2&&(last==0||now-last>=COOLDOWN);}
    boolean shouldPreload(long now){return count>=1&&(last==0||now-last>=COOLDOWN-15000);}
    void shown(long now){count=0;last=now;}
    int count(){return count;}
    long last(){return last;}
}
