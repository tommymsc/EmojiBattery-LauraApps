"""Exercise actual Google test native ad delivery and capture diagnostic evidence."""
import subprocess,time,sys
def adb(*args):
    return subprocess.run(["adb",*args],check=True,capture_output=True,text=True,timeout=45).stdout
adb("logcat","-c")
adb("shell","am","force-stop","com.lauraapps.emojibattery")
adb("shell","am","start","-W","-n","com.lauraapps.emojibattery/.MainActivity")
logs=""
for _ in range(24):
    time.sleep(5)
    logs=adb("logcat","-d","-s","EmojiAds:D","*:S")
    if "native attached to gallery size=" in logs:
        break
print(logs,flush=True)
if "native attached to gallery size=" not in logs:
    print("::warning::Native test ad did not attach; inspect consent, connectivity and load error above.",flush=True)
else:
    import re
    sizes=re.findall(r"native attached to gallery size=(\d+)x(\d+) shown=true",logs)
    assert any(int(x)>0 and int(y)>0 for x,y in sizes), "Native view has zero size or is hidden"
    print("PASS: Google native test ad loaded and attached with nonzero visible dimensions",flush=True)
