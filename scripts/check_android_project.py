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
assert re.search(r'versionCode\s+5',gradle)
source=(app/'java/com/lauraapps/emojibattery/BatteryArt.java').read_text()
assert 'ThemeCatalog.species(t)' in source
catalog=(app/'java/com/lauraapps/emojibattery/ThemeCatalog.java').read_text()
assert 'COUNT=24' in catalog and 'SPECIES=12' in catalog
print('PASS: 24 designs, bilingual resource parity, API 36 and no sensitive permissions')
