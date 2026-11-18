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
}