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
    tree=hierarchy()
    # Recover only from a known external launcher ANR; never dismiss an app ANR.
    if any(n.get("text")=="Pixel Launcher isn't responding" for n in tree.iter("node")):
        for n in tree.iter("node"):
            if n.get("resource-id")=="android:id/aerr_close":
                x1,y1,x2,y2=map(int,re.findall(r"-?\d+",n.get("bounds","")))
                adb("shell","input","tap",str((x1+x2)//2),str((y1+y2)//2))
                print("Recovered from external Pixel Launcher ANR",flush=True)
                time.sleep(2)
                adb("shell","am","start","-W","-n","com.lauraapps.emojibattery/.MainActivity","--ez","qa_skip_ads","true")
                tree=hierarchy()
                break
    for n in tree.iter("node"):
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
adb("shell","am","start","-W","-n","com.lauraapps.emojibattery/.MainActivity","--ez","qa_skip_ads","true")
# Wait for the initial gallery without swiping away from its top controls.
for attempt in range(15):
    if locate("edit_selected_mascot"):
        break
    time.sleep(2)
else:
    print(adb("shell","dumpsys","activity","activities"))
    print(ET.tostring(hierarchy(),encoding="unicode"))
    print(adb("logcat","-d","-t","300"))
    raise AssertionError("Initial gallery did not become ready")
tap("edit_selected_mascot","up")
assert locate("back_to_gallery"), "Editor did not open"
tap("automatic_battery_mode")
assert "✓" in seek("automatic_battery_mode")[0], "Automatic mode did not activate"
tap("fixed_battery_mode","up")
assert "✓" in seek("fixed_battery_mode","up")[0], "Fixed mode did not activate"
tap("battery_preview_15")
assert locate("battery_preview_15"), "Preview controls disappeared"
tap("battery_preview_100")
tap("battery_preview_-1")
# Return to live battery and verify actual Android broadcasts, not just simulated buttons.
try:
    adb("shell","dumpsys","battery","unplug")
    adb("shell","dumpsys","battery","set","status","3")
    for level in (82, 19, 7, 63):
        adb("shell","dumpsys","battery","set","level",str(level))
        status=seek("battery_status","up")
        for attempt in range(10):
            if str(level)+"%" in status[0]:
                break
            time.sleep(2)
            status=seek("battery_status","up")
        assert str(level)+"%" in status[0], f"Live battery mismatch: expected {level}%, got {status[0]}"
    adb("shell","dumpsys","battery","set","ac","1")
    adb("shell","dumpsys","battery","set","status","2")
    for attempt in range(10):
        if "Charging" in seek("battery_status","up")[0]:
            break
        time.sleep(2)
    else:
        raise AssertionError("Charging state not updated")
finally:
    adb("shell","dumpsys","battery","reset")
# Capture selected theme then switch language while the editor is open.
seek("battery_preview_15","down")
selected=[n.get("content-desc","") for n in hierarchy().iter("node") if n.get("content-desc","").startswith("selected_mascot_")]
# The selected title may be just above the visible preview controls.
if not selected:
    adb("shell","input","swipe",str(width//2),str(int(height*.3)),str(width//2),str(int(height*.65)),"350")
    time.sleep(.5)
    selected=[n.get("content-desc","") for n in hierarchy().iter("node") if n.get("content-desc","").startswith("selected_mascot_")]
assert selected, "Selected mascot title missing"
tap("language_selector","up")
def pick_language(label):
    for n in hierarchy().iter("node"):
        if n.get("text")==label:
            x1,y1,x2,y2=map(int,re.findall(r"\d+",n.get("bounds","")))
            adb("shell","input","tap",str((x1+x2)//2),str((y1+y2)//2))
            time.sleep(2)
            return
    raise AssertionError("Language missing: "+label)
pick_language("Español")
assert "Volver" in seek("back_to_gallery","up")[0], "Language change left editor"
assert seek(selected[0],"down"), "Selected mascot changed"
tap("language_selector","up")
pick_language("English")
assert "Back to gallery" in seek("back_to_gallery","up")[0]
tap("back_to_gallery","up")
assert seek("edit_selected_mascot","up"), "Gallery did not reopen"
assert adb("shell","pidof","com.lauraapps.emojibattery").strip(), "App stopped"
print("PASS: gallery/editor navigation, automatic and fixed mode, preview level controls, return to gallery")
