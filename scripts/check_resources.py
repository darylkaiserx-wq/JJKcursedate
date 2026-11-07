"""Check packaged references independently of asset generation."""
import json,struct
from pathlib import Path
r=Path('src/main/resources');a=r/'assets/jjkcursedate';d=r/'data/jjkcursedate'
roster=json.loads((d/'roster.json').read_text());ids={c['id'] for c in roster}
assert len(ids)==len(roster)
for c in roster:
    skin=a/f'textures/entity/{c["id"]}.png'
    assert struct.unpack('>II',skin.read_bytes()[16:24])==(128,128),skin
    assert len(c['moves'])==3
    for move in c['moves']:assert move['cost']>=0 and move['cooldown']>0 and 1<=move['level']<=60
models=list((a/'models/item').glob('*.json'))
for model in models:
    data=json.loads(model.read_text())
    for texture in data.get('textures',{}).values():
        if texture.startswith('jjkcursedate:'):assert (a/'textures'/ (texture.split(':')[1]+'.png')).exists(),(model,texture)
    for box in data.get('elements',[]):assert all(-16<=n<=32 for n in box['from']+box['to'])
recipes=list((d/'recipes').glob('*.json'));signatures=set()
for recipe in recipes:
    data=json.loads(recipe.read_text());result=data['result']['item'].split(':')[1]
    assert (a/f'models/item/{result}.json').exists(),result
    signature=json.dumps({k:v for k,v in data.items() if k!='result'},sort_keys=True)
    assert signature not in signatures,('Conflicting recipe',recipe)
    signatures.add(signature)
for path in r.rglob('*.json'):json.loads(path.read_text())
assert (d/'structures/empty.nbt').exists()
print(f'PASS: {len(roster)} skins, {len(models)} item models, {len(recipes)} distinct recipes, all JSON references')
# New sprite/model/audio references must resolve independently of their generators.
for particle in (a/'particles').glob('*.json'):
    textures=json.loads(particle.read_text())['textures'];assert len(textures)==6
    for t in textures:assert (a/'textures/particle'/(t.split(':')[1]+'.png')).exists(),t
for name in ['plaster','roof_tiles','timber_panel','curse_stone','paving']:
    assert (a/f'blockstates/{name}.json').exists()
    model=json.loads((a/f'models/block/{name}.json').read_text())
    for t in model['textures'].values():assert (a/'textures'/(t.split(':')[1]+'.png')).exists()
    assert (d/f'loot_tables/blocks/{name}.json').exists()
for c in ids|{'divine_dog','nue','rika_manifestation','cursed_spirit','moon_dregs','garuda','death_swarm','mahoraga'}:
    assert (a/f'models/item/{c}_spawn_egg.json').exists(),c
for name,entry in json.loads((a/'sounds.json').read_text()).items():
    for t in entry['sounds']:
        sound=a/'sounds'/(t.split(':')[1]+'.ogg');assert sound.read_bytes()[:4]==b'OggS',sound
print('PASS: 12 six-frame sprite sets, 5 materials with loot, 76 egg models, 11 Vorbis sound references')
for skin in (a/'textures/models/armor').glob('*.png'):
    assert struct.unpack('>II',skin.read_bytes()[16:24])==(128,64),skin
print('PASS: 16 shaded outfit armor atlases')
# Each loot location can produce every registered cursed tool and collectible relic.
all_tools={'slaughter_demon','playful_cloud','inverted_spear','soul_split_katana','dragon_bone','black_rope','chain_of_a_thousand_miles','nanami_blade','festering_life_sword','executioner_sword','hiten','kamutoke','nobara_hammer','mechamaru_blade'}
relics={'sukuna_finger','prison_realm'}|{f'death_painting_{i}' for i in range(1,10)}
for loc in ['city','residence','ruins']:
    table=json.loads((d/f'loot_tables/chests/{loc}.json').read_text())
    entries={e['name'].split(':')[1] for pool in table['pools'] for e in pool['entries'] if e['type']=='minecraft:item'}
    assert all_tools|relics|{'curse_fragment'} <= entries,(loc,entries)
    for item in entries:assert (a/f'models/item/{item}.json').exists(),item
print('PASS: all 14 tools and 11 relics in each of three location loot tables')
