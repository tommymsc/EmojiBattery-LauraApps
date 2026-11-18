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
assert '<uses-permission' not in (app/'AndroidManifest.xml').read_text()
gradle=(root/'app/build.gradle').read_text()
assert re.search(r'targetSdk\s+36',gradle)
assert re.search(r'versionCode\s+6',gradle)
source=(app/'java/com/lauraapps/emojibattery/BatteryArt.java').read_text()
assert 'ThemeCatalog.species(t)' in source
catalog=(app/'java/com/lauraapps/emojibattery/ThemeCatalog.java').read_text()
assert 'COUNT=24' in catalog and 'SPECIES=12' in catalog
print('PASS: 24 designs, bilingual resource parity, API 36 and no sensitive permissions')

import base64,hashlib
encoded=''.join((app/'assets'/('mascot_atlas.part'+str(i))).read_text().strip() for i in range(1,5))
assert len(encoded)==16540, f'Atlas encoded length unexpected: {len(encoded)}'
asset=base64.b64decode(encoded,validate=True)
assert hashlib.sha256(asset).hexdigest()=='182b08a7d2fa96cd22326b15f477c193fc5c281df2e18110e4497c3ca94c42e1', 'Asset bytes do not match approved image'
assert asset.startswith(b'RIFF') and b'WEBP' in asset[:16]
assert b'mascot_atlas.part' in (app/'java/com/lauraapps/emojibattery/BatteryArt.java').read_bytes()
print('PASS: original glossy mascot atlas verified, 12 characters / 24 themes')
