import re
from pathlib import Path
from xml.etree import ElementTree as ET
root=Path(__file__).resolve().parents[1]
app=root/'app/src/main'
sets=[]
for loc in ('res/values/strings.xml','res/values-es/strings.xml'):
    names=[n.get('name') for n in ET.parse(app/loc).getroot() if n.tag=='string']
    assert len(names)==len(set(names)),loc+' has duplicate keys'
    sets.append(set(names))
assert sets[0]==sets[1],f'Unequal English and Spanish translations: {sets[0]^sets[1]}'
assert all('mascot_'+str(i) in sets[0] for i in range(24))
manifest=(app/'AndroidManifest.xml').read_text()
assert manifest.count('<uses-permission')==1
assert 'android.permission.SET_WALLPAPER' in manifest
assert 'android.permission.BIND_DREAM_SERVICE' in manifest
assert '.BatteryDreamService' in manifest
assert 'MANAGE_EXTERNAL_STORAGE' not in manifest and 'SYSTEM_ALERT_WINDOW' not in manifest
gradle=(root/'app/build.gradle').read_text()
assert re.search(r'targetSdk\s+36',gradle)
assert re.search(r'versionCode\s+7',gradle)
source=(app/'java/com/lauraapps/emojibattery/BatteryArt.java').read_text()
assert 'ThemeCatalog.species(t)' in source
catalog=(app/'java/com/lauraapps/emojibattery/ThemeCatalog.java').read_text()
assert 'COUNT=24' in catalog and 'SPECIES=12' in catalog
# Protect the minimal-permissions design and the in-app privacy notice.
assert 'android.permission.INTERNET' not in manifest
for forbidden in ('READ_CONTACTS','ACCESS_FINE_LOCATION','QUERY_ALL_PACKAGES',
                  'REQUEST_INSTALL_PACKAGES','POST_NOTIFICATIONS','READ_MEDIA_IMAGES',
                  'BIND_ACCESSIBILITY_SERVICE','FOREGROUND_SERVICE',
                  'USE_FULL_SCREEN_INTENT','SCHEDULE_EXACT_ALARM'):
    assert forbidden not in manifest, 'Unexpected permission: '+forbidden
assert 'android:permission="android.permission.BIND_DREAM_SERVICE"' in manifest
assert 'android:exported="true"' in manifest
assert 'android:allowBackup="false"' in manifest
assert "testImplementation 'junit:junit:4.13.2'" in gradle
for key in ('privacy_read','privacy_full_title','privacy_full_body'):
    assert key in sets[0], 'Missing in-app privacy declaration: '+key
activity=(app/'java/com/lauraapps/emojibattery/MainActivity.java').read_text()
assert 'R.string.privacy_full_body' in activity
assert (root/'docs/privacy.html').is_file(), 'Missing public privacy page source'
print('PASS: policy guards for SDK, permissions, privacy notice and exported services')
print('PASS: 24 designs, bilingual resource parity, API 36 and no sensitive permissions')

import base64,hashlib
from pathlib import Path
encoded=''.join((app/'assets'/('mascot_atlas_hd.part'+str(i))).read_text().strip() for i in range(1,5))
assert len(encoded)==374432, f'HD atlas encoded length unexpected: {len(encoded)}'
asset=base64.b64decode(encoded,validate=True)
assert hashlib.sha256(asset).hexdigest()=='8c6687d0cf03d80405f12cb7e99cee5cebd6119e2b82012a8a754ffb7c3fa205', 'Asset bytes do not match approved image'
assert asset.startswith(b'RIFF') and b'WEBP' in asset[:16]
assert b'mascot_atlas_hd.part' in (app/'java/com/lauraapps/emojibattery/BatteryArt.java').read_bytes()
print('PASS: original glossy mascot atlas verified, 12 characters / 24 themes')
