package com.lauraapps.emojibattery;
import org.junit.Test;
import static org.junit.Assert.*;
public class BatteryMathTest {
 @Test public void percentageBoundaries(){
  assertEquals(0,BatteryMath.percent(0,100));
  assertEquals(47,BatteryMath.percent(47,100));
  assertEquals(100,BatteryMath.percent(200,100));
  assertEquals(-1,BatteryMath.percent(-1,100));
  assertEquals(-1,BatteryMath.percent(4,0));
 }
 @Test public void moodBoundaries(){
  assertEquals(0,BatteryMath.mood(15,false));
  assertEquals(1,BatteryMath.mood(35,false));
  assertEquals(2,BatteryMath.mood(65,false));
  assertEquals(3,BatteryMath.mood(90,false));
  assertEquals(4,BatteryMath.mood(12,true));
 }
 @Test public void automaticMascotThresholds(){
  assertEquals(7,BatteryMath.automaticTheme(0,false,3));
  assertEquals(7,BatteryMath.automaticTheme(20,false,3));
  assertEquals(9,BatteryMath.automaticTheme(21,false,3));
  assertEquals(9,BatteryMath.automaticTheme(45,false,3));
  assertEquals(6,BatteryMath.automaticTheme(46,false,3));
  assertEquals(6,BatteryMath.automaticTheme(79,false,3));
  assertEquals(10,BatteryMath.automaticTheme(80,false,3));
  assertEquals(10,BatteryMath.automaticTheme(100,false,3));
  assertEquals(1,BatteryMath.automaticTheme(10,true,3));
  assertEquals(3,BatteryMath.automaticTheme(-1,false,3));
  assertEquals(23,BatteryMath.automaticTheme(-1,false,40));
 }
 @Test public void newMascotIdsRemainStable(){
  assertEquals(84,ThemeCatalog.TOTAL);
  for(int old=0;old<84;old++)assertEquals(old,ThemeCatalog.clamp(old));
  assertEquals(1000,ThemeCatalog.newTheme(0,0));
  for(int i=0;i<ThemeCatalog.NEW_COUNT;i++){
   for(int p=0;p<ThemeCatalog.PALETTES;p++){
    int id=ThemeCatalog.newTheme(i,p);
    assertEquals(id,ThemeCatalog.clamp(id));
    assertEquals(i,ThemeCatalog.newIndex(id));
    assertEquals(p,ThemeCatalog.palette(id));
    assertEquals(ThemeCatalog.newTheme(i,6),ThemeCatalog.withPalette(id,6));
    assertEquals("mascots/"+ThemeCatalog.NEW_MASCOTS[i]+".webp",ThemeCatalog.assetName(id));
    assertTrue(ThemeCatalog.gallerySelected(ThemeCatalog.defaultNewTheme(i),id));
   }
  }
 }
}