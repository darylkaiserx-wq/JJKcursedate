"""Non-skin art pass: sculpted tools/relics, architecture, fabric and animated effects.
Uses Pillow 13. Deliberately never writes textures/entity (including summon skins).
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter,PngImagePlugin
import json,math,random,os
A=Path('src/main/resources/assets/jjkcursedate')
def rgb(h):return tuple(bytes.fromhex(h))+(255,)
def shade(c,n):return tuple(max(0,min(255,int(x+n))) for x in c[:3])+(c[3] if len(c)==4 else 255,)
def save(im,p):
 p.parent.mkdir(parents=True,exist_ok=True);meta=PngImagePlugin.PngInfo();meta.add_text('Software','CodeRabbit generated; scripts/visual_assets.py');im.save(p,pnginfo=meta)
def js(p,d):p.write_text(json.dumps(d,indent=2)+'\n')
def box(a,b,t='main',rotation=None):
 x,y,z=[b[i]-a[i] for i in range(3)];faces={}
 for f,w,h in [('north',x,y),('south',x,y),('east',z,y),('west',z,y),('up',x,z),('down',x,z)]:
  faces[f]={'texture':'#'+t,'uv':[0,0,min(16,max(.5,w)),min(16,max(.5,h))]}
 out={'from':a,'to':b,'faces':faces}
 if rotation:out['rotation']={'origin':[(a[i]+b[i])/2 for i in range(3)],'axis':rotation[0],'angle':rotation[1]}
 return out
# Metals show a bevel, fine brush lines and distinct patina; handles have braided wraps.
tools='slaughter_demon playful_cloud inverted_spear soul_split_katana dragon_bone black_rope chain_of_a_thousand_miles nanami_blade festering_life_sword executioner_sword hiten kamutoke nobara_hammer mechamaru_blade'.split()
accents='527c9d a83236 d3c9ae 75695e b49862 302a44 8793a3 85967b 847b4c f2b750 c19a66 b9b7a8 a47653 536c85'.split()
for idx,name in enumerate(tools):
 textures={}
 for material,color in {'steel':'647585','edge':'d1e0e8','gold':'c49b50','grip':'302737','accent':accents[idx]}.items():
  im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);rng=random.Random(name+material);base=rgb(color)
  for y in range(64):
   for x in range(64):
    light=14*math.sin(x*math.pi/64)+rng.randrange(-4,5)+(9 if x%16 in [0,1] else 0)
    if material=='grip':light+=12 if (x+y*2)%16<4 else -3
    if material in ['steel','edge']:light+=5*math.sin(y*2.3)
    im.putpixel((x,y),shade(base,light))
  if material=='gold':
   for y in range(4,64,16):d.line((5,y,32,y+6,58,y),fill=shade(base,35),width=1)
  if material=='accent' and name in ['nanami_blade','festering_life_sword','soul_split_katana']:
   for y in range(8,64,16):
    for x in range(8,64,16):d.ellipse((x-3,y-3,x+2,y+2),fill=shade(base,-40));d.point((x-1,y-2),fill=shade(base,20))
  save(im,A/f'textures/item/{name}_{material}.png');textures[material]=f'jjkcursedate:item/{name}_{material}'
 e=[]
 def add(a,b,t='steel',rot=None):e.append(box(a,b,t,rot))
 # Chamfered handles and individual wrap bands.
 if name not in ['playful_cloud','black_rope','chain_of_a_thousand_miles']:
  add([7.2,0,7.2],[8.8,7,8.8],'grip');add([7,0,7],[9,1,9],'gold')
  for y in range(1,7):add([7,y,7],[9,y+.25,9],'accent')
 if name=='playful_cloud':
  for i,(x,y) in enumerate([(6,0),(9,11),(6,22)]):
   add([x,y,7],[x+2, y+8.5,9],'accent')
   for h in [0,1,7.5,8]:add([x-.2,y+h,6.8],[x+2.2,y+h+.5,9.2],'gold')
  for x,y in [(7,8.5),(9,19.5)]:
   add([x,y,7.5],[x+.5,y+2.5,8.5]);add([x+1,y,7.5],[x+1.5,y+2.5,8.5]);add([x,y,7.5],[x+1.5,y+.4,8.5]);add([x,y+2.1,7.5],[x+1.5,y+2.5,8.5])
 elif name in ['black_rope','chain_of_a_thousand_miles']:
  for i in range(10):
   x=7+math.sin(i*.8)*3;y=i*2.8
   if name=='black_rope':
    add([x-.6,y,7],[x+.6,y+3.1,8.2],'grip',('z',22.5 if i%2 else -22.5))
    add([x-.8,y+.5,6.8],[x+.8,y+1,8.4],'accent')
   else:
    # Alternate link planes so the chain reads as interlocked geometry.
    if i%2:
     for z in [6.8,8.5]:add([x-.3,y,z],[x+.3,y+3,z+.45])
     for h in [0,2.6]:add([x-.3,y+h,6.8],[x+.3,y+h+.4,8.95],'edge')
    else:
     for xx in [x-1,x+.6]:add([xx,y,7.5],[xx+.4,y+3,8.1])
     for h in [0,2.6]:add([x-1,y+h,7.5],[x+1,y+h+.4,8.1],'edge')
 elif name in ['nobara_hammer','kamutoke']:
  add([7.25,7,7.25],[8.75,20,8.75],'grip');add([3,18,6],[12,22,10],'accent')
  add([2,18.4,6.3],[3,21.6,9.7],'edge');add([11.8,18.4,6.3],[13,21.6,9.7],'steel')
  if name=='nobara_hammer':
   add([12,19,6.5],[15,20,7.3],'steel');add([12,19,8.7],[15,20,9.5],'steel');add([6,22,7.1],[9,22.4,8.9],'gold')
  else:
   for x in [3,7,11]:add([x,21,7],[x+1,25,9],'gold');add([x+.1,25,7.2],[x+.9,27,8.8],'edge')
 elif name in ['inverted_spear','hiten']:
  add([7.35,7,7.35],[8.65,19,8.65],'grip');add([4,18,7],[12,19,9],'gold')
  add([7,19,7.5],[9,29,8.5]);add([7.5,29,7.5],[8.5,31,8.5],'edge')
  for x in [4,11]:add([x,19,7.4],[x+1,26,8.6]);add([x+.2,26,7.4],[x+.8,28,8.6],'edge')
  if name=='inverted_spear':add([3.5,23,7.3],[7,24,8.7],'edge');add([9,21,7.3],[12.5,22,8.7],'edge')
 elif name=='nanami_blade':
  add([6,6,6.5],[10,8,9.5],'grip');add([5.5,8,7],[10.5,24,9],'accent');add([5,8,7],[5.5,23,9],'edge');add([5.5,24,7],[9.5,25,9],'accent')
  for y in range(9,24,2):add([5.4,y,6.9],[10.6,y+.3,9.1],'grip')
 elif name=='dragon_bone':
  add([5,6,6],[11,8,10],'gold');add([5,8,7],[10,26,9],'accent');add([4.3,10,7.2],[5,25,8.8],'edge')
  for y in [9,13,17]:
   add([9,y,6],[12,y+2,10],'steel');add([10,y+.3,5.7],[11.5,y+1.7,6],'gold')
  add([6,26,7],[9,28,9],'edge')
 else:
  add([4.5,6,6],[11.5,7,10],'gold')
  if name in ['soul_split_katana','slaughter_demon','mechamaru_blade']:
   for i in range(5):
    x=7+i*.25;y=7+i*4
    add([x,y,7.5],[x+1.6,y+4.1,8.5]);add([x-.3,y,7.45],[x,y+4.1,8.55],'edge')
   add([8.2,27,7.5],[8.8,29,8.5],'edge')
   if name=='soul_split_katana':
    for y in [7,8,9]:add([5.5,y,6],[10.5,y+.4,10],'grip')
  else:
   add([7,7,7.5],[9,28,8.5]);add([6.5,8,7.4],[7,28,8.6],'edge');add([7,28,7.5],[8,30,8.5],'edge')
  if name=='festering_life_sword':
   for y in [11,16,21]:add([6,y,6],[10,y+2,10],'accent');add([7,y+.5,5.7],[9,y+1.5,6],'gold')
  if name=='executioner_sword':add([3,20,7.25],[13,21,8.75],'accent');add([6.7,10,7.1],[7.3,27,7.4],'gold')
 textures['particle']=textures['steel']
 display={'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,1,0],'scale':[.65]*3},'thirdperson_lefthand':{'rotation':[0,90,0],'translation':[0,1,0],'scale':[.65]*3},'firstperson_righthand':{'rotation':[0,-90,15],'translation':[1,-1,-1],'scale':[.65]*3},'firstperson_lefthand':{'rotation':[0,90,-15],'translation':[1,-1,-1],'scale':[.65]*3},'gui':{'rotation':[20,20,-38],'translation':[0,-2,0],'scale':[.57]*3},'ground':{'rotation':[0,0,90],'scale':[.3]*3},'fixed':{'rotation':[0,180,-35],'scale':[.55]*3}}
 js(A/f'models/item/{name}.json',{'textures':textures,'elements':e,'display':display})
# Relics: sculpted finger, layered eye cube and sealed fetal jars.
for path in [A/'models/item/sukuna_finger.json',A/'models/item/prison_realm.json']+list((A/'models/item').glob('death_painting_*.json')):
 name=path.stem;model=json.loads(path.read_text());e=[]
 def add(a,b):e.append(box(a,b))
 if name=='sukuna_finger':
  for i in range(4):add([6+i*.18,1+i*3,6+i*.1],[10-i*.18,4.2+i*3,10-i*.1])
  add([6.7,12,6.7],[9.3,15,9.3]);add([6.9,12.6,6.3],[9.1,14.8,6.7])
  for i in range(3):add([5.5,2+i*3.2,7],[6.2,2.5+i*3.2,8])
 elif name=='prison_realm':
  add([2.5,2.5,2.5],[13.5,13.5,13.5])
  for x in [2,12.8]:
   for z in [2,12.8]:add([x,2,z],[x+1.2,14,z+1.2])
  for y in [2,12.8]:add([2,y,2],[14,y+1.2,14])
  # Eye plaques use full-face UVs instead of repeating a tiny cut of the atlas.
  for a,b in [([4,5,2.1],[12,11,2.5]),([4,5,13.5],[12,11,13.9]),([2.1,5,4],[2.5,11,12]),([13.5,5,4],[13.9,11,12])]:add(a,b)
 else:
  add([5,2,5],[11,3,11]);add([4,3,4],[12,12,12]);add([5,12,5],[11,14,11]);add([5.5,14,5.5],[10.5,15,10.5])
  for y in [3,11,13]:add([3.8,y,3.8],[12.2,y+.5,12.2])
  add([7,5,3.7],[9,11,4]);add([6,8,3.6],[10,9,4.1])
 # All relic faces use the entire painted face, so eyes/seals remain readable.
 for cube in e:
  for face in cube['faces'].values():face['uv']=[0,0,16,16]
 model['elements']=e;js(path,model)
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);base=rgb('b07b65' if name=='sukuna_finger' else '716676' if name=='prison_realm' else '63424e');rng=random.Random(name)
 for y in range(64):
  for x in range(64):im.putpixel((x,y),shade(base,rng.randrange(-7,8)+int(math.sin(x*.1)*9)))
 if name=='prison_realm':
  d.rounded_rectangle((2,2,61,61),8,outline=rgb('bfa987'),width=4);d.ellipse((9,19,55,48),fill=rgb('392a39'),outline=rgb('d8b18c'),width=3);d.ellipse((24,21,40,46),fill=rgb('99324d'),outline=rgb('e1ba92'),width=2);d.ellipse((29,24,35,43),fill=rgb('1d2030'));d.ellipse((29,24,32,28),fill=rgb('ffe6bd'))
  for x,y in [(10,9),(45,8),(8,51),(48,51)]:d.line((x,y,x+6,y+4,x+2,y+6),fill=rgb('3d343e'),width=2)
 elif name=='sukuna_finger':
  for y in [15,33,49]:d.arc((5,y-6,58,y+7),0,180,fill=rgb('684137'),width=3);d.line((12,y+8,38,y+10),fill=rgb('c19a7a'))
  d.polygon([(13,3),(52,6),(46,22),(17,20)],fill=rgb('443440'),outline=rgb('c4a187'));d.line((20,5,45,8),fill=rgb('826b71'),width=2)
 else:
  d.rounded_rectangle((4,3,59,60),9,outline=rgb('ad9685'),width=3);d.ellipse((14,15,51,51),fill=rgb('6f303f'),outline=rgb('ae6968'),width=2);d.arc((22,21,44,44),10,310,fill=rgb('e3b18a'),width=4)
  d.rectangle((26,5,39,59),fill=rgb('d1b995'));d.line((30,13,35,20,29,26,35,33,30,40,34,48),fill=rgb('492b35'),width=2)
 save(im,A/f'textures/item/{name}.png')
# Seamless architecture: material grain is deterministic; geometry gives tiles/panels depth.
for name,color in {'plaster':'ded7c6','roof_tiles':'303b54','timber_panel':'63503e','curse_stone':'3d354c','paving':'8f9194'}.items():
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);rng=random.Random(name);base=rgb(color)
 for y in range(64):
  for x in range(64):
   grain=rng.randrange(-5,6)+(math.sin(x*.8+math.sin(y*.1)*2)*5 if name=='timber_panel' else 0)
   im.putpixel((x,y),shade(base,grain))
 if name=='plaster':
  for y in [0,32]:
   d.line((0,y,63,y),fill=rgb('b4ac9e'));d.line((0,y+1,63,y+1),fill=rgb('eee7d7'))
  for i in range(9):
   x=rng.randrange(64);y=rng.randrange(64);d.line((x,y,x+2,y+2,x+1,y+5),fill=rgb('c9c0ae'))
 elif name=='roof_tiles':
  for y in range(0,64,16):
   d.line((0,y,63,y),fill=rgb('131e32'),width=2);d.line((0,y+3,63,y+3),fill=rgb('53627c'))
   for x in range(0,64,16):d.line((x,y,x,y+15),fill=rgb('192639'),width=2);d.line((x+3,y+4,x+3,y+14),fill=rgb('6b7790'))
 elif name=='timber_panel':
  for x in range(0,64,16):d.line((x,0,x,63),fill=rgb('302b2c'),width=2);d.line((x+2,0,x+2,63),fill=rgb('8c7255'))
  for x,y in [(9,18),(40,45)]:d.ellipse((x-3,y-6,x+3,y+6),outline=rgb('44332c'));d.ellipse((x-1,y-3,x+1,y+3),fill=rgb('352b28'))
 elif name=='curse_stone':
  d.rectangle((2,2,61,61),outline=rgb('74647f'),width=2);d.line((32,8,53,32,32,56,11,32,32,8),fill=rgb('9b8aac'),width=2)
  for x,y in [(12,12),(48,12),(12,48),(48,48)]:d.line((x-3,y,x+3,y,x,y+5,x,y-5),fill=rgb('baa3c3'),width=1)
  d.ellipse((26,26,38,38),outline=rgb('c7acd7'),width=2)
 else:
  for y in range(0,64,32):
   d.line((0,y,63,y),fill=rgb('545a64'),width=2)
   for x in range(-16 if y else 0,64,32):d.line((x,y,x,y+31),fill=rgb('5d626a'),width=2);d.line((x+2,y+2,x+28,y+2),fill=rgb('adb0b2'))
 save(im,A/f'textures/block/{name}.png')
 model={'textures':{'all':f'jjkcursedate:block/{name}','particle':f'jjkcursedate:block/{name}'},'elements':[]}
 if name=='roof_tiles':
  model['elements']=[box([0,0,0],[16,14.5,16],'all')]
  for z in range(0,16,4):
   for x in range(0,16,4):model['elements'].append(box([x+.1,14.5,z+.1],[x+3.9,15.6+(z%8)*.1,z+3.9],'all'))
 elif name=='timber_panel':
  model['elements']=[box([.25,0,.25],[15.75,16,15.75],'all')]
  for x in [0,7.5,15]:
   for z in [0,15.5]:model['elements'].append(box([x,0,z],[x+1,16,z+.5],'all'))
 else:model={'parent':'minecraft:block/cube_all','textures':{'all':f'jjkcursedate:block/{name}'}}
 js(A/f'models/block/{name}.json',model)
# Tailored armor atlases, independent of character skins. Vanilla 64x32 UV scaled 2x.
outfits={'tokyo_uniform':'222a44','kyoto_uniform':'273c40','gojo_outfit':'171c30','sukuna_robes':'d8d2c2','toji_outfit':'29332c','yuta_uniform':'e3ded1','nanami_suit':'b2a07e','geto_robes':'332d45'}
for name,color in outfits.items():
 for layer in [1,2]:
  im=Image.new('RGBA',(128,64));rng=random.Random(name);base=rgb(color)
  for y in range(64):
   for x in range(128):im.putpixel((x,y),shade(base,rng.randrange(-3,4)+(2 if (x+y)%4==0 else 0)))
  d=ImageDraw.Draw(im)
  # body front (40,40)-(56,64), boot fronts (8,40)-(16,64), sleeve fronts (88,40)-(96,64)
  for x0,x1 in [(40,56),(8,16),(88,96)]:
   d.line((x0,40,x0,63),fill=shade(base,-25),width=2);d.line((x1-1,40,x1-1,63),fill=shade(base,24))
   d.line((x0,61,x1,61),fill=shade(base,-30),width=2)
  d.line((47,40,47,62),fill=shade(base,-35),width=2)
  if name in ['geto_robes','sukuna_robes']:
   d.polygon([(40,40),(44,40),(54,60),(51,63)],fill=rgb('b6a16f') if name=='geto_robes' else rgb('514655'))
  elif name=='nanami_suit':
   d.polygon([(42,40),(48,46),(54,40)],fill=rgb('e5dccc'));d.polygon([(47,44),(50,44),(50,59),(48,61)],fill=rgb('45695a'))
  else:
   for y in [43,50,57]:d.ellipse((49,y,51,y+2),fill=rgb('c1a269'),outline=rgb('67503c'))
   d.line((42,45,45,45),fill=shade(base,32));d.line((42,46,42,50),fill=shade(base,-18))
  # Boots with soles and laces; layer two carries the trousers.
  for x in [0,8,16,24]:
   if layer==1:d.rectangle((x,56,x+7,63),fill=rgb('252b35'));d.line((x,62,x+7,62),fill=rgb('565361'))
  # Clear the face region on helmets; the top rim remains visible, no replacement face.
  d.rectangle((16,18,31,31),fill=(0,0,0,0));d.rectangle((16,16,31,18),fill=shade(base,8))
  save(im,A/f'textures/models/armor/{name}_layer_{layer}.png')
 # Matching inventory clothing silhouettes, replacing flat placeholder squares.
 for part in ['helmet','chestplate','leggings','boots']:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);c=base
  if part=='chestplate':d.polygon([(9,5),(4,7),(1,16),(7,18),(9,13),(9,28),(23,28),(23,13),(25,18),(31,16),(28,7),(23,5),(20,10),(12,10)],fill=c,outline=shade(c,30));d.line((16,11,16,27),fill=shade(c,-35),width=2)
  elif part=='leggings':d.polygon([(7,5),(25,5),(26,28),(18,28),(16,16),(14,28),(6,28)],fill=c,outline=shade(c,30));d.line((8,9,24,9),fill=shade(c,-35),width=2)
  elif part=='boots':
   for x in [5,18]:d.polygon([(x,9),(x+8,9),(x+8,27),(x-2,27),(x-2,23),(x,22)],fill=rgb('343642'),outline=rgb('88838a'))
  else:d.polygon([(7,21),(7,9),(12,5),(21,5),(26,10),(26,21),(22,21),(21,13),(12,13),(11,21)],fill=c,outline=shade(c,40))
  save(im,A/f'textures/item/{name}_{part}.png')
# Utility ingredients: book binding, crystal fragment, wrapped hilt.
for name in ['fate_book','curse_fragment','wrapped_hilt']:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if name=='fate_book':
  d.polygon([(5,4),(23,2),(28,7),(28,28),(9,30),(4,25)],fill=rgb('292942'),outline=rgb('c4ad76'));d.polygon([(24,5),(27,7),(27,25),(24,27)],fill=rgb('d4c6ac'));d.rectangle((7,6,22,26),outline=rgb('85704c'));d.line((15,9,21,16,15,23,9,16,15,9),fill=rgb('ae85ce'),width=2)
 elif name=='curse_fragment':
  d.polygon([(9,26),(4,16),(12,3),(23,5),(28,18),(20,29)],fill=rgb('47375c'),outline=rgb('b0a0d8'));d.polygon([(12,3),(14,18),(23,5)],fill=rgb('a18bc8'));d.polygon([(14,18),(28,18),(20,29)],fill=rgb('6e5893'));d.line((5,16,14,18,20,29),fill=rgb('d1b9ea'))
 else:
  d.rounded_rectangle((11,3,21,29),2,fill=rgb('302c37'),outline=rgb('bc9a62'))
  for y in range(5,29,4):d.line((12,y,20,y+3),fill=rgb('88808c'),width=2)
 # Resolve actual book icon filename from item model if this old placeholder was renamed.
 if (A/f'textures/item/{name}.png').exists():save(im,A/f'textures/item/{name}.png')
# Utility items are small held sculptures, not generated flat quads.
for material,color in {'book_cover':'25243c','book_pages':'d8cdb3','fragment_core':'756093'}.items():
 im=Image.new('RGBA',(64,64));rng=random.Random(material);base=rgb(color);d=ImageDraw.Draw(im)
 for y in range(64):
  for x in range(64):im.putpixel((x,y),shade(base,rng.randrange(-4,5)+(6 if material=='book_pages' and y%4==0 else 0)))
 if material=='book_cover':d.rectangle((3,3,60,60),outline=rgb('bca16c'),width=3);d.line((32,10,52,32,32,54,12,32,32,10),fill=rgb('a17bc2'),width=3)
 save(im,A/f'textures/item/{material}.png')
for name in ['fate_book','curse_fragment']:
 if name=='fate_book':
  elements=[box([2,2,5],[14,3,12],'cover'),box([2,13,5],[14,14,12],'cover'),box([2,3,5],[3,13,12],'cover'),box([3,3,5.5],[13.5,13,11.5],'pages')]
  tex={'cover':'jjkcursedate:item/book_cover','pages':'jjkcursedate:item/book_pages','particle':'jjkcursedate:item/book_cover'}
  # The front cover faces the viewer, embossed with the existing book illustration.
  elements=[box([2,2,5],[14,14,5.8],'cover'),box([2,2,11.2],[14,14,12],'cover'),box([2,2,5.8],[3,14,11.2],'cover'),box([3,2.5,5.8],[13.5,13.5,11.2],'pages')]
  for e in elements:
   for face in e['faces'].values():face['uv']=[0,0,16,16]
 elif name=='curse_fragment':
  elements=[box([5,2,5],[11,13,11],'core',('z',22.5)),box([3,5,6],[7,11,10],'core',('z',-22.5)),box([9,7,6],[13,12,10],'core',('z',22.5))]
  tex={'core':'jjkcursedate:item/fragment_core','particle':'jjkcursedate:item/fragment_core'}
 js(A/f'models/item/{name}.json',{'textures':tex,'elements':elements,'display':{'gui':{'rotation':[20,30,-15],'scale':[.8]*3},'firstperson_righthand':{'rotation':[0,-35,0],'scale':[.65]*3},'thirdperson_righthand':{'rotation':[0,0,0],'scale':[.6]*3},'ground':{'scale':[.4]*3}}})

# Summon material atlases retain the original face UV region. Humanoid skin files are never touched.
for form in ['divine_dog','nue','rika_manifestation','cursed_spirit','moon_dregs','garuda','death_swarm','mahoraga']:
 source=Image.open(A/f'textures/entity/summon_{form}.png').convert('RGBA')
 im=source.resize((128,128),Image.Resampling.NEAREST);d=ImageDraw.Draw(im);rng=random.Random(form)
 for y in range(32,128):
  for x in range(128):
   c=im.getpixel((x,y));grain=rng.randrange(-3,4)+int(math.sin(x*.16)*7)
   if form in ['nue','garuda']:grain+=9 if (x//8+y//12)%2==0 else -6
   elif form=='divine_dog':grain+=6 if (x+y*3)%11<3 else -2
   elif form in ['rika_manifestation','mahoraga']:grain+=int(math.sin(x*.12+math.sin(y*.12))*10)
   im.putpixel((x,y),shade(c,grain))
 # Head rectangle stays byte-equivalent after nearest scaling; body contours use 64px UV landmarks.
 if form in ['mahoraga','rika_manifestation','cursed_spirit']:
  for y in [66,74,82]:d.arc((8,y-6,47,y+7),8,170,fill=rgb('a7a497'),width=2)
  d.line((27,62,27,94),fill=rgb('b5b2a6'),width=2)
 elif form in ['nue','garuda']:
  for y in range(98,128,8):
   for x in range(2,92,12):d.line((x,y,x+5,y+6,x+10,y),fill=rgb('83716d') if form=='nue' else rgb('bccaa5'),width=2)
 elif form=='moon_dregs':
  for x in range(4,120,10):d.line((x,55,x+2,90,x,124),fill=rgb('a191c7'),width=1)
 elif form=='death_swarm':
  for y in range(40,120,10):d.line((68,y,99,y),fill=rgb('687c8d'))
 if form=='mahoraga':
  d.rectangle((96,0,127,31),fill=rgb('bc9c55'))
  for x in range(98,128,7):d.line((x,0,x,31),fill=rgb('e0ce8f'),width=2)
 save(im,A/f'textures/models/summon/{form}.png')

# Six distinct frames per effect: transparent cores/ribbons, no opaque rectangular backgrounds.
fx=A/'textures/particle'
palette={'black_flash':'ff1637','blood_orb':'c9203b','blue_orb':'23aeff','red_orb':'ff2b44','purple_orb':'a15eff','slash':'d1eeff','fire':'ff8e29','ice':'a4edff','lightning':'b9fff4','curse':'b782df','heal':'91ffd3','barrier':'ffd892'}
for kind,color in palette.items():
 for frame in range(6):
  im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);c=rgb(color);rng=random.Random(kind+str(frame));phase=frame/6*math.pi*2
  if kind=='black_flash':
   sheet=Image.open('art/references/blackflashanim.png').convert('RGBA');raw=sheet.crop((0,frame*512,512,(frame+1)*512)).resize((128,128),Image.Resampling.LANCZOS);pixels=[(255,15,36,min(255,r*2)) if r>40 else (0,0,0,0) for r,g,b,a in raw.get_flattened_data()];raw.putdata(pixels);im.alpha_composite(raw);d=ImageDraw.Draw(im)
  elif kind in ['slash','lightning']:
   if kind=='slash':
    pts=[(8,105),(27,62),(59,31),(113,9),(75,29),(40,65)];d.polygon([(x,y+math.sin(phase)*4) for x,y in pts],fill=c[:3]+(240-frame*15,));d.line([(12,98),(34,60),(70,26),(113,9)],fill=(255,255,255,245),width=1+frame%3)
   else:
    pts=[(8+i*14,64+rng.randrange(-22,23)) for i in range(9)];d.line(pts,fill=c,width=5);d.line(pts,fill=(245,255,255,255),width=2)
    for i in [2,5]:x,y=pts[i];d.line((x,y,x+12,y-22,x+8,y-32),fill=c,width=2)
  elif kind=='fire':
   for i in range(7):
    x=27+i*11;top=12+rng.randrange(22);d.polygon([(x-16,105),(x-20,68),(x,top),(x+8,65),(x+18,94)],fill=c[:3]+(180,))
   d.polygon([(43,105),(40,76),(61,43),(71,75),(86,104)],fill=(255,228,159,255))
  elif kind=='ice':
   for i in range(6):
    a=i*math.pi/3+phase*.08;d.line((64,64,64+math.cos(a)*48,64+math.sin(a)*48),fill=c,width=3)
    for r in [23,37]:
     x=64+math.cos(a)*r;y=64+math.sin(a)*r
     for off in [-.7,.7]:d.line((x,y,x-math.cos(a+off)*13,y-math.sin(a+off)*13),fill=c,width=2)
  elif kind=='heal':
   d.rounded_rectangle((52,25,76,103),3,fill=c);d.rounded_rectangle((25,52,103,76),3,fill=c);d.rectangle((59,31,65,95),fill=(230,255,246,220))
  else:
   for radius in range(48,3,-2):d.ellipse((64-radius,64-radius,64+radius,64+radius),fill=c[:3]+(int(110*(1-radius/50)),))
   for j in range(3):
    r=30+j*7;start=frame*35+j*100;d.arc((64-r,64-r,64+r,64+r),start,start+140,fill=c,width=3)
   if kind=='blood_orb':d.ellipse((37,37,91,91),fill=(132,9,34,240),outline=c,width=3);d.ellipse((43,43,58,53),fill=(255,150,159,240))
   elif kind=='barrier':
    points=[(64+math.cos(i*math.pi/3+phase*.06)*44,64+math.sin(i*math.pi/3+phase*.06)*44) for i in range(7)];d.line(points,fill=c,width=3)
   else:d.ellipse((53,53,75,75),fill=(246,232,255,250))
  glow=im.filter(ImageFilter.GaussianBlur(3));glow.alpha_composite(im);save(glow,fx/f'{kind}_{frame}.png')
# Art board uses actual atlas pixels rather than imagined renders.
board=Image.new('RGB',(960,500),(19,23,34));d=ImageDraw.Draw(board);d.text((22,14),'CURSED FATE / MATERIAL & EFFECT ATLAS 0.5.0',fill=(225,214,188))
for i,name in enumerate(['plaster','roof_tiles','timber_panel','curse_stone','paving']):
 image=Image.open(A/f'textures/block/{name}.png').resize((128,128),Image.Resampling.NEAREST);board.paste(image,(22+i*185,45));d.text((22+i*185,181),name,fill=(196,207,222))
for i,kind in enumerate(palette):
 image=Image.open(fx/f'{kind}_2.png');x=20+(i%6)*156;y=215+(i//6)*140;board.paste(image,(x,y),image);d.text((x,y+116),kind,fill=(196,207,222))
save(board,Path(os.environ.get('WORKSPACE_ROOT','.'))/'output/jjkcursedate/materials-0.5.0.png')
print('Redesigned 14 tool models, 11 relics, 5 building materials, 16 armor atlases, 32 outfit icons and 72 effect frames; entity skins untouched')
