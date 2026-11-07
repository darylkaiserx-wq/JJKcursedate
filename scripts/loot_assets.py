"""Generate cursed-object models, textures, translations and location loot tables (Pillow)."""
import json,random
from pathlib import Path
from PIL import Image,ImageDraw,PngImagePlugin
A=Path('src/main/resources/assets/jjkcursedate');D=Path('src/main/resources/data/jjkcursedate')
def write(path,value):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,indent=2)+'\n')
def texture(name,base,variant):
 im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im);r=random.Random(name)
 for _ in range(100):
  x=r.randrange(16);y=r.randrange(16);v=r.randrange(-15,16);d.point((x,y),fill=tuple(max(0,min(255,c+v)) for c in base[:3])+(255,))
 if variant=='finger':
  d.rectangle((1,1,14,14),outline=(97,35,37));d.line((2,5,13,5),fill=(111,52,45));d.line((2,10,13,10),fill=(111,52,45));d.line((4,3,9,2),fill=(235,182,144),width=2)
 elif variant=='prison':
  d.rectangle((1,1,14,14),outline=(155,124,103),width=2);d.ellipse((3,4,12,12),fill=(63,39,50),outline=(216,179,145));d.ellipse((6,5,9,11),fill=(160,42,48));d.point((7,7),fill=(245,222,182))
 else:
  d.rectangle((1,1,14,14),outline=(90,24,34),width=2);d.ellipse((3,3,12,12),outline=(210,126,104),width=2)
  for i in range(int(variant)):d.point((4+(i%3)*3,5+(i//3)*3),fill=(244,195,141))
 path=A/f'textures/item/{name}.png';path.parent.mkdir(parents=True,exist_ok=True);meta=PngImagePlugin.PngInfo();meta.add_text('Software','CodeRabbit generated; loot_assets.py');im.save(path,pnginfo=meta)
def box(a,b):return {'from':a,'to':b,'faces':{f:{'texture':'#main','uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}}
relics=['sukuna_finger','prison_realm']+[f'death_painting_{i}' for i in range(1,10)]
lang=json.loads((A/'lang/en_us.json').read_text())
for n in relics:
 variant='finger' if n=='sukuna_finger' else 'prison' if n=='prison_realm' else n.rsplit('_',1)[1]
 texture(n,(185,125,106,255) if variant=='finger' else (105,84,86,255) if variant=='prison' else (142,57,60,255),variant)
 elements=([box([6,1,6],[10,14,10]),box([6.5,14,6.5],[9.5,16,9.5])] if variant=='finger' else [box([2,2,2],[14,14,14])] if variant=='prison' else [box([4,3,4],[12,13,12]),box([6,13,6],[10,15,10])])
 display={'gui':{'rotation':[25,45,0],'scale':[.75,.75,.75]},'ground':{'scale':[.45,.45,.45]},'thirdperson_righthand':{'rotation':[0,0,0],'scale':[.55,.55,.55]},'firstperson_righthand':{'rotation':[0,45,0],'scale':[.6,.6,.6]}}
 write(A/f'models/item/{n}.json',{'textures':{'main':f'jjkcursedate:item/{n}','particle':f'jjkcursedate:item/{n}'},'elements':elements,'display':display})
 lang['item.jjkcursedate.'+n]="Sukuna's Finger" if n=='sukuna_finger' else 'Prison Realm' if n=='prison_realm' else 'Cursed Womb: Death Painting '+variant
lang['item.jjkcursedate.relic_description']='Collectible cursed object'
write(A/'lang/en_us.json',lang)
tools=['slaughter_demon','playful_cloud','inverted_spear','soul_split_katana','dragon_bone','black_rope','chain_of_a_thousand_miles','nanami_blade','festering_life_sword','executioner_sword','hiten','kamutoke','nobara_hammer','mechamaru_blade']
def item(name,weight=1):return {'type':'minecraft:item','name':'jjkcursedate:'+name,'weight':weight}
def pool(entries,chance=None):
 p={'rolls':1,'entries':entries}
 if chance is not None:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
 return p
for location,chance,relic_chance in [('city',.55,.12),('residence',.65,.2),('ruins',1,.45)]:
 fragments=item('curse_fragment');fragments['functions']=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':4}}]
 data={'type':'minecraft:chest','pools':[pool([fragments]),pool([item(t,3 if t in ['slaughter_demon','nobara_hammer','nanami_blade','mechamaru_blade'] else 1) for t in tools],chance),pool([item(n) for n in relics],relic_chance)]}
 write(D/f'loot_tables/chests/{location}.json',data)
print('Generated 11 3D collectible relics and three cursed-item chest tables')
