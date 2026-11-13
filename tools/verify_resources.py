"""Validate custom geometry, asset links, enchanting and damage resources."""
import json, struct
from pathlib import Path
root = Path(__file__).resolve().parents[1] / 'src/main/resources'
def read(rel):
    return json.loads((root / rel).read_text(encoding='utf-8-sig'))
for path in root.rglob('*'):
    if path.suffix in ('.json', '.mcmeta'):
        json.loads(path.read_text(encoding='utf-8-sig'))
items = ['reality_sword', 'immortal_helmet', 'immortal_chestplate', 'immortal_leggings', 'immortal_boots']
for item in items:
    model = read(f'assets/realityimmortal/models/item/{item}.json')
    assert len(model['elements']) >= (40 if item == 'reality_sword' else 10)
    for texture in model['textures'].values():
        namespace, name = texture.split(':')
        image = root / f'assets/{namespace}/textures/{name}.png'
        assert image.read_bytes()[:8] == b'\x89PNG\r\n\x1a\n'
    for element in model['elements']:
        assert all(-16 <= x <= 32 for x in element['from'] + element['to'])
        assert all(a < b for a,b in zip(element['from'],element['to']))
        for face in element['faces'].values():
            assert face['texture'][1:] in model['textures']
        if 'rotation' in element:
            assert element['rotation']['angle'] in [-45,-22.5,0,22.5,45]
    for language in ['en_us','vi_vn']:
        strings=read(f'assets/realityimmortal/lang/{language}.json')
        assert f'item.realityimmortal.{item}' in strings

for item,tag in zip(items,['sword','head_armor','chest_armor','leg_armor','foot_armor']):
    assert f'realityimmortal:{item}' in read(f'data/minecraft/tags/item/enchantable/{tag}.json')['values']
for layer in [1,2]:
    png=(root/f'assets/realityimmortal/textures/models/armor/immortal_layer_{layer}.png').read_bytes()
    assert struct.unpack('>II',png[16:24]) == (128,128)
for tag in ['bypasses_armor','bypasses_shield','bypasses_invulnerability','bypasses_cooldown','bypasses_resistance','bypasses_enchantments','bypasses_effects']:
    assert 'realityimmortal:reality_cut' in read(f'data/minecraft/tags/damage_type/{tag}.json')['values']
print('PASS: five 3D item models, geometry bounds/rotations, texture references, 128px armor atlases, enchantment/damage tags')
