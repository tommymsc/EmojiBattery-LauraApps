package com.lauraapps.emojibattery;
import org.junit.Test;
import static org.junit.Assert.*;
public class BatteryMathTest {
  @Test public void clampsLevels(){assertEquals(0,BatteryMath.percent(0,100));assertEquals(50,BatteryMath.percent(1,2));assertEquals(100,BatteryMath.percent(200,100));assertEquals(-1,BatteryMath.percent(-1,100));assertEquals(-1,BatteryMath.percent(50,0));}
  @Test public void moods(){assertEquals(0,BatteryMath.mood(10,false));assertEquals(1,BatteryMath.mood(45,false));assertEquals(2,BatteryMath.mood(95,false));assertEquals(3,BatteryMath.mood(50,true));}
}
