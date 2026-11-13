"""Validate resource links, enchantment coverage and PNG layouts without dependencies."""
import json
import struct
from pathlib import Path

root = Path(__file__).resolve().parents[1] / 'src/main/resources'
for path in root.rglob('*.json'):
    json.loads(path.read_text(encoding='utf-8-sig'))
for path in root.rglob('*.mcmeta'):
    json.loads(path.read_text(encoding='utf-8-sig'))

def read(relative):
    return json.loads((root / relative).read_text(encoding='utf-8-sig'))

items = ['reality_sword', 'immortal_helmet', 'immortal_chestplate', 'immortal_leggings', 'immortal_boots']
for item in items:
    model = read(f'assets/realityimmortal/models/item/{item}.json')
    texture = model['textures']['layer0']
    namespace, name = texture.split(':')
    image = root / f'assets/{namespace}/textures/{name}.png'
    png = image.read_bytes()
    assert png[:8] == b'\x89PNG\r\n\x1a\n', image
    width, height = struct.unpack('>II', png[16:24])
    assert (width, height) == ((32, 128) if item == 'reality_sword' else (32, 32)), image
    for language in ['en_us', 'vi_vn']:
        assert f'item.realityimmortal.{item}' in read(f'assets/realityimmortal/lang/{language}.json')

for item, tag in zip(items, ['sword', 'head_armor', 'chest_armor', 'leg_armor', 'foot_armor']):
    assert f'realityimmortal:{item}' in read(f'data/minecraft/tags/item/enchantable/{tag}.json')['values']
for layer in [1, 2]:
    png = (root / f'assets/realityimmortal/textures/models/armor/immortal_layer_{layer}.png').read_bytes()
    assert struct.unpack('>II', png[16:24]) == (64, 32)
for tag in ['bypasses_armor', 'bypasses_shield', 'bypasses_invulnerability', 'bypasses_cooldown', 'bypasses_resistance', 'bypasses_enchantments', 'bypasses_effects']:
    assert 'realityimmortal:reality_cut' in read(f'data/minecraft/tags/damage_type/{tag}.json')['values']
assert read('data/realityimmortal/damage_type/reality_cut.json')['scaling'] == 'never'
print('PASS: JSON, 5 custom item models, animated sword, both armor layers, enchantment and damage tags')
