"""UI smoke test: swipe the screen and actually toggle automatic mascot mode."""
import re
import subprocess
import time
import xml.etree.ElementTree as ET

def adb(*args):
    result = subprocess.run(["adb", *args], check=True, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, text=True, timeout=35)
    return result.stdout

def hierarchy():
    for attempt in range(3):
        try:
            adb("shell", "uiautomator", "dump", "/sdcard/emoji-window.xml")
            return ET.fromstring(adb("shell", "cat", "/sdcard/emoji-window.xml"))
        except (subprocess.SubprocessError, ET.ParseError):
            if attempt == 2:
                raise
            time.sleep(2)

def find_toggle(tree):
    for node in tree.iter("node"):
        if "Automatic with battery" in node.get("text", ""):
            bounds = re.findall(r"\d+", node.get("bounds", ""))
            if len(bounds) == 4:
                x1, y1, x2, y2 = map(int, bounds)
                if x2 > x1 and y2 > y1 and y2 > 0 and y1 < 2200:
                    return node.get("text", ""), (x1+x2)//2, (y1+y2)//2
    return None

adb("shell", "am", "start", "-W", "-n",
    "com.lauraapps.emojibattery/.MainActivity")
time.sleep(3)
initial = hierarchy()
item = find_toggle(initial)
for _ in range(7):
    if item is not None:
        break
    adb("shell", "input", "swipe", "450", "1540", "450", "580", "450")
    time.sleep(1)
    item = find_toggle(hierarchy())
assert item is not None, "Automatic mode button not reachable through vertical swipes"
text, x, y = item
if "✓" not in text:
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(2)
    updated = find_toggle(hierarchy())
    assert updated is not None and "✓" in updated[0], "Automatic mode tap did not update UI"
else:
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(2)
    updated = find_toggle(hierarchy())
    assert updated is not None and "✓" not in updated[0], "Mode toggle tap did not update UI"
adb("shell", "input", "swipe", "450", "600", "450", "1500", "430")
time.sleep(1)
assert adb("shell", "pidof", "com.lauraapps.emojibattery").strip(), "App stopped after interaction"
print("PASS: scroll down/up, tap mascot mode, UI state changes and app remains alive")
