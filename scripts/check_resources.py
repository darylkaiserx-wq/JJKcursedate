"""Check packaged references independently of asset generation."""
import json,struct
from pathlib import Path
r=Path('src/main/resources');a=r/'assets/jjkcursedate';d=r/'data/jjkcursedate'
roster=json.loads((d/'roster.json').read_text());ids={c['id'] for c in roster}
assert len(ids)==len(roster)
for c in roster:
    skin=a/f'textures/entity/{c["id"]}.png'
    assert struct.unpack('>II',skin.read_bytes()[16:24])==(64,64),skin
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
