import json, collections, struct
from pathlib import Path
root=Path('src/main/resources')
def read(p): return json.loads((root/p).read_text(encoding='utf-8-sig'))
recipe=read('data/realityimmortal/recipe/reality_sword.json')
counts=collections.Counter(recipe['key'][c]['item'] for row in recipe['pattern'] for c in row if c!=' ')
assert counts=={'realityimmortal:reality_fragment':1,'minecraft:netherite_sword':4,'realityimmortal:divinity_ore':4}
assert recipe['result']=={'id':'realityimmortal:reality_sword','count':1}
assert read('data/realityimmortal/recipe/reality_fragment.json')['result']['id']=='realityimmortal:reality_fragment'
for part in ['helmet','chestplate','leggings','boots']:
    png=(root/f'assets/realityimmortal/textures/item/immortal_{part}.png').read_bytes()
    assert struct.unpack('>II',png[16:24])==(64,64)
assert read('assets/realityimmortal/models/item/divinity_ore.json')['parent']=='realityimmortal:block/divinity_ore'
assert read('data/realityimmortal/neoforge/biome_modifier/divinity_ore.json')['step']=='underground_ores'
assert read('data/realityimmortal/worldgen/configured_feature/divinity_ore.json')['config']['size']==4
print('PASS: exact 1 fragment + 4 swords + 4 ore recipe, 64px icons, ore resources and worldgen')
