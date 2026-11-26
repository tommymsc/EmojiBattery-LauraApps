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
assert re.search(r'versionCode\s+15',gradle)
source=(app/'java/com/lauraapps/emojibattery/BatteryArt.java').read_text()
assert 'ThemeCatalog.species(t)' in source
catalog=(app/'java/com/lauraapps/emojibattery/ThemeCatalog.java').read_text()
assert 'COUNT=24' in catalog and 'SPECIES=12' in catalog
assert 'auto_mode' in catalog and 'automaticTheme' in catalog
assert 'mode_heading' in sets[0] and 'mode_fixed' in sets[0] and 'mode_auto' in sets[0]
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

# Verify all individually packaged mascots against their stable catalog.
import json
manifest=json.loads((root/'mascot-manifest.json').read_text())
assert len(manifest)==70
assert [m['index'] for m in manifest]==list(range(len(manifest)))
assert len({m['id'] for m in manifest})==len(manifest)
for m in manifest:
    assert m['theme_id']==1000+m['index']*7
    assert ('cat_'+m['id'] if m['index']<14 else m['id']) in sets[0]
    raw=(app/'assets'/'mascots'/(m['id']+'.webp')).read_bytes()
    assert len(raw)==m['bytes']
    assert hashlib.sha1(b'blob '+str(len(raw)).encode()+b'\0'+raw).hexdigest()==m['git_sha']
    assert raw[:4]==b'RIFF' and raw[8:12]==b'WEBP' and raw[12:16]==b'VP8X'
    assert raw[20]&0x10,'Expected transparency flag'
    assert int.from_bytes(raw[24:27],'little')+1==512
    assert int.from_bytes(raw[27:30],'little')+1==512
    assert '"'+m['id']+'"' in catalog
print('PASS: all 70 individual transparent WebP mascots, stable IDs and bilingual names')

assert 'KITTEN_COUNT=14' in catalog and 'DOG_COUNT=7' in catalog
assert 'R.string.filter_dogs' in activity
assert len(list((app/'assets'/'mascots').glob('*.webp')))==len(manifest)
assert all('R.string.'+('cat_'+m['id'] if m['index']<14 else m['id']) in activity for m in manifest)

assert 'BEAR_COUNT=6' in catalog and 'R.string.filter_bears' in activity

assert 'FRUIT_COUNT=7' in catalog and 'R.string.filter_fruits' in activity

assert 'FANTASY_COUNT=6' in catalog and 'R.string.filter_fantasy' in activity

assert 'FOOD_COUNT=7' in catalog and 'R.string.filter_food' in activity

assert 'AQUATIC_COUNT=6' in catalog and 'SPACE_COUNT=7' in catalog
assert 'R.string.filter_aquatic' in activity and 'R.string.filter_space' in activity
assert 'HorizontalScrollView categoryScroll' in activity

assert 'EMOTION_COUNT=7' in catalog and 'NATURE_COUNT=3' in catalog
assert 'R.string.filter_emotions' in activity and 'R.string.filter_nature' in activity

assert 'galleryLimit=16' in activity and 'R.string.gallery_more' in activity
assert len(re.findall(r'"[a-z_]+"', re.search(r'NEW_MASCOTS=\{([^}]+)\}',catalog).group(1)))==70
assert len(re.search(r'NEW_DEFAULTS=\{([^}]+)\}',catalog).group(1).split(','))==70

assert 'if(widgetCount()>0)' in activity and 'PIN_COOLDOWN_MS' in activity
assert 'widget_duplicate_notice' in sets[0] and 'widget_update_existing' in sets[0]
