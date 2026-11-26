"""Exercise gallery/editor navigation, real preview and both battery modes."""
import re
import subprocess
import time
import xml.etree.ElementTree as ET

def adb(*args):
    return subprocess.run(["adb", *args], check=True, stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE, text=True, timeout=45).stdout

def hierarchy():
    for attempt in range(4):
        try:
            adb("shell", "uiautomator", "dump", "/sdcard/emoji-window.xml")
            return ET.fromstring(adb("shell", "cat", "/sdcard/emoji-window.xml"))
        except (subprocess.SubprocessError, ET.ParseError):
            if attempt == 3:
                raise
            time.sleep(1)

sizes = re.findall(r"(\d+)x(\d+)", adb("shell", "wm", "size"))
width, height = map(int, sizes[-1])
def locate(key):
    for n in hierarchy().iter("node"):
        if n.get("content-desc") != key:
            continue
        coords = re.findall(r"-?\d+", n.get("bounds", ""))
        if len(coords) != 4:
            continue
        x1,y1,x2,y2 = map(int,coords)
        x1,y1,x2,y2 = max(0,x1),max(0,y1),min(width,x2),min(height,y2)
        if x2-x1>10 and y2-y1>18:
            return n.get("text",""),(x1+x2)//2,(y1+y2)//2
    return None

def seek(key, direction="down", attempts=10):
    for _ in range(attempts):
        result=locate(key)
        if result:
            return result
        start,end=(int(height*.80),int(height*.30)) if direction=="down" else (int(height*.30),int(height*.80))
        adb("shell","input","swipe",str(width//2),str(start),str(width//2),str(end),"400")
        time.sleep(.7)
    raise AssertionError("Control not reachable: "+key)

def tap(key,direction="down"):
    _,x,y=seek(key,direction)
    adb("shell","input","tap",str(x),str(y))
    time.sleep(1)

adb("shell","am","force-stop","com.lauraapps.emojibattery")
adb("shell","am","start","-W","-n","com.lauraapps.emojibattery/.MainActivity")
time.sleep(3)
tap("edit_selected_mascot")
assert locate("back_to_gallery"), "Editor did not open"
tap("automatic_battery_mode")
assert "✓" in seek("automatic_battery_mode")[0], "Automatic mode did not activate"
tap("fixed_battery_mode","up")
assert "✓" in seek("fixed_battery_mode","up")[0], "Fixed mode did not activate"
tap("battery_preview_15")
assert locate("battery_preview_15"), "Preview controls disappeared"
tap("battery_preview_100")
tap("battery_preview_-1")
tap("back_to_gallery","up")
assert seek("edit_selected_mascot","up"), "Gallery did not reopen"
assert adb("shell","pidof","com.lauraapps.emojibattery").strip(), "App stopped"
print("PASS: gallery/editor navigation, automatic and fixed mode, preview level controls, return to gallery")
