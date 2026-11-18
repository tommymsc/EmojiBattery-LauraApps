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
}