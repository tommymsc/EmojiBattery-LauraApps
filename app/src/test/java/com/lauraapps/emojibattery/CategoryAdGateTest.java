package com.lauraapps.emojibattery;
import org.junit.Test;
import static org.junit.Assert.*;
public class CategoryAdGateTest {
 @Test public void thirdVisitEligible(){CategoryAdGate g=new CategoryAdGate(0,0);g.visit();assertFalse(g.shouldPreload(100));g.visit();assertTrue(g.shouldPreload(100));assertFalse(g.eligible(100));g.visit();assertTrue(g.eligible(100));}
 @Test public void cooldownAndReset(){CategoryAdGate g=new CategoryAdGate(3,0);g.shown(1000);assertEquals(0,g.count());g.visit();g.visit();g.visit();assertFalse(g.eligible(60999));assertTrue(g.eligible(61000));}
 @Test public void missedAdKeepsEligibility(){CategoryAdGate g=new CategoryAdGate(3,0);g.visit();assertEquals(3,g.count());assertTrue(g.eligible(100));}
}
