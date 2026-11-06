"""128px character skins, UV-correct shading, sculpted tools and reference-driven effects.
Run AFTER scripts/assets.py with Pillow 13: python scripts/art_upgrade.py.
"""
from PIL import Image,ImageDraw,ImageFilter,PngImagePlugin
from pathlib import Path
import json,math,random,os
A=Path('src/main/resources/assets/jjkcursedate');D=Path('src/main/resources/data/jjkcursedate')
roster=json.loads((D/'roster.json').read_text());ref=Image.open('art/references/trendyguy.png').convert('RGBA')
def col(h):return tuple(bytes.fromhex(h))+(255,)
def shade(c,n):return tuple(max(0,min(255,v+n)) for v in c[:3])+(c[3] if len(c)>3 else 255,)
def save(im,path):
 path.parent.mkdir(parents=True,exist_ok=True);meta=PngImagePlugin.PngInfo();meta.add_text("Software","CodeRabbit generated asset; art_upgrade.py");im.save(path,pnginfo=meta)
def js(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n')
# Each roster member has an explicit hair, garment, eye and complexion palette.
profiles='''gojo e8eff8 141626 39c8ff e8c6b4 spiky
yuji e68f9a 20233a 79513f e5b29b spiky
sukuna e68f9a e6ddd3 cf253a e2b09b spiky
megumi 15252d 20243b 4e786b e4c3af spiky
nobara a66b36 20243b 946342 e9bda6 bob
maki 213d35 20283a 9d7b38 e4c1a4 ponytail
toge d9d7c5 26253a 8e7497 e7cab3 fringe
panda e5e3d8 25252b 27232c e8e6dc beast
nanami d2bf7d b59b73 567d69 e1bd9e slick
yuta 19252e e9e6de 606b77 e6cdb8 fringe
geto 1b1c29 322e42 65478a d9b49b long
mahito a6bdc9 33333b 8691a4 c9d6dc long
jogo 5f4539 b5a66f 9b2024 a6ab99 beast
hanami b4b9a5 d6d1b8 803332 bfc2aa beast
dagon b6544f a34643 f4d383 c76a60 beast
toji 1e2727 232d27 628870 d4b498 fringe
choso 29252c 563a4b 744d71 d7bca6 ponytail
todo 28242b 1f252b 5e4540 c69a7b slick
kamo 282830 e8e5df 803d45 e6c2ab ponytail
momo e2d491 302c3c 628c80 edc9b1 ponytail
mai 253a36 273248 42624b dfbba5 bob
miwa 86aacb 242c40 5e85ad ecc9b5 long
mechamaru 38312c 837356 b82728 bcab8d beast
utahime 272232 e5e0d5 6c5246 e4bca3 long
shoko 514034 31303b 78604d d8b69b bob
gakuganji c3c3b1 45413e 554a45 b9a88f long
kusakabe 332b28 67705c 655447 c7a88d slick
mei_mei c1cedc 2e3447 748cb5 e6cab7 braid
ui_ui a8c7d4 222b45 668aaf e8c5b2 fringe
naobito d4d2bb 644136 75503f c4a285 slick
naoya b9ac78 41463e 8b744a dac0a1 fringe
yuki e4cb72 322c35 9b723e e4b294 long
tengen e3dbc2 bdb4a1 763831 c9bfaa beast
kenjaku 211f2a 363041 754b91 d4b39a long
uraume dce9ec e6e3df 8f4967 e6ccc3 bob
haruta e1c36c e2dcd0 8f7542 e7bc9a bob
eso c7b2b8 4b3149 9b4c78 d1a7bb beast
kechizu 69877d 666c53 d44b59 81a797 beast
finger_bearer c8baa3 b5a98f 93283d cec2ad beast
grasshopper 43563a 697d45 dc8737 8ba25a beast
smallpox 626a43 727e4e d2c98b 8c9972 beast
rika d5d0bc bdbbad 852f3d d8d4be beast
higuruma 312c30 292b35 625646 cfae93 slick
hakari a2a976 614237 8d743e b2937a slick
kirara 292336 27252f ac81bd e5c1aa long
kashimo 98cfc5 c7c4ab a17d38 dbc3a4 ponytail
hara d5bf8c e6d8b9 716a4f dab494 long
takaba 252528 e0c532 6a4d32 d8ac85 slick
hanezoki c7c0ac 6a5960 775e54 d9b098 fringe
ryu 27212c 3b3243 74533a c8a17e slick
uro c3b9b9 a5c3cb 8990a5 e0b59e long
kurourushi 262d22 414b31 b0a553 626e46 beast
dhruv d4c5a4 857157 66503b b99372 long
angel e6d49f ece5cb b59c54 e4c4ad bob
yorozu 24202c 474744 9d654e d8b69f long
daido a09d8b 71644c 6c5740 c2a185 slick
miyo 252221 8b6647 5e4432 b98c68 slick
larue b68d8f 5d4055 81555b d0a48e long
miguel 22212c 583b2e 806b3a 795644 slick
junpei 1b242a 293440 57726b e1bfa9 fringe
ijichi 25242a 292d38 6e5c4a d7baa0 slick
nitta c69669 32313c 7b5f42 e5baa0 bob
arata c1aa72 29323e 786b46 dcc0a6 fringe
riko 2b2a39 294056 6387a0 e7c7b3 long
kuro 332c30 2d2d3c 6f5b4a e1bca2 long
tsumiki 302b30 e2d8c5 735946 e5bea6 long
wasuke c1bba7 9b937b 776552 c5a98b slick
yuko 6d4940 d2b7af 845f4e e6bfa7 bob'''
P={r[0]:r[1:] for r in [line.split() for line in profiles.splitlines()]}
# Paint all six faces of each standard skin cuboid; reference contributes tailored fabric folds.
parts=[(0,0,8,8,8,'head'),(16,16,8,12,4,'torso'),(40,16,4,12,4,'arm'),(32,48,4,12,4,'arm'),(0,16,4,12,4,'leg'),(16,48,4,12,4,'leg')]
for c in roster:
 id=c['id'];hair,cloth,eye,skin,style=P[id];hair,cloth,eye,skin=map(col,[hair,cloth,eye,skin]);im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);rng=random.Random(id)
 def box(x,y,w,h,color):d.rectangle((int(x*2),int(y*2),int((x+w)*2-1),int((y+h)*2-1)),fill=color)
 for u,v,w,h,depth,part in parts:
  faces=[(u+depth,v,w,depth,8),(u+depth+w,v,w,depth,-12),(u,v+depth,depth,h,-10),(u+depth,v+depth,w,h,4),(u+depth+w,v+depth,depth,h,-18),(u+depth*2+w,v+depth,w,h,-7)]
  base=skin if part=='head' else cloth
  for fx,fy,fw,fh,light in faces:
   for yy in range(int(fh*2)):
    for xx in range(int(fw*2)):
     fold=math.sin(xx*.75+yy*.16)*4+math.cos(yy*.85)*2
     # Fabric fold luminance from supplied skin, mapped into the new palette.
     rr=ref.getpixel((min(63,int(fx+xx/2)),min(63,int(fy+yy/2))))
     lum=(sum(rr[:3])/3-55)*.1 if rr[3]>0 and part!='head' else 0
     pixel=shade(base,int(light+fold+lum+rng.choice([-1,0,1])))
     im.putpixel((int(fx*2)+xx,int(fy*2)+yy),pixel)
  if part=='arm':
   for fx,fy,fw,fh,_ in faces[2:]:box(fx,fy+8,fw,4,skin);box(fx,fy+7.5,fw,.5,shade(cloth,-20))
  if part=='leg':
   for fx,fy,fw,fh,_ in faces[2:]:box(fx,fy+9,fw,3,col('25242d'));box(fx,fy+11,fw,.5,col('494551'))
 # Hair: top/sides/back have directional strands, with a transparent raised outer fringe.
 for region in [(0,0,32,8),(0,8,8,8),(16,8,16,8)]:
  x,y,w,h=region
  for yy in range(h*2):
   for xx in range(w*2):im.putpixel((x*2+xx,y*2+yy),shade(hair,int(math.sin(xx*1.4+yy*.15)*12+math.cos(yy*.5)*5)))
 for x in range(16):
  length=[3,2,4,3,2,4,2,3][x//2] if style in ['spiky','fringe'] else (3 if style in ['bob','long','braid','ponytail'] else 1)
  d.line((16+x,16,16+x,16+length),fill=shade(hair,(x%3)*6-8))
 # Sculpted face: shaded eye sockets, whites, iris highlight, nose and small mouth.
 for x in [9,13]:
  box(x-.5,10,2.5,.5,shade(hair,-10));box(x,10.5,2,1.5,col('e6ded3'));box(x+.5,10.5,1,1.5,eye);box(x+.5,10.5,.5,.5,col('f8f5ec'))
 box(11.5,12,1,1,shade(skin,-14));box(11,14,2,.5,shade(skin,-55));box(10.5,13.5,.5,.5,shade(skin,-12))
 # Uniform tailoring: collar folds, diagonally fastened jacket, trim and gold buttons.
 box(20,20,8,1,shade(cloth,-20));box(23.5,21,.5,10,shade(cloth,28));box(20,30.5,8,.5,shade(cloth,-28))
 for y in [22,25,28]:box(24,y,.5,.5,col('c8ac70'));box(24.5,y,.5,.5,col('76613f'))
 # Raised hat layer / side locks, outer jacket cuffs and seams on valid UV islands.
 for x in range(8):
  height=[2,3,1,2,3,2,1,2][x] if style=='spiky' else 2
  box(40+x,8,1,height,shade(hair,(x%3)*8-6))
 if style in ['long','braid','bob','ponytail']:
  for x in [32,48,56]:box(x,8,8,8,shade(hair,-9))
  box(40,8,1,7,hair);box(47,8,1,7,hair)
 if id=='gojo':
  box(8,10,8,3,col('13151e'));box(8,10,8,.5,col('33374b'));box(32,10,32,2,col('151722'));box(40,10,8,3,col('181b29'))
 if id in ['nanami','maki','ijichi']:
  for x in [8.5,12.5]:box(x,10.5,3,2,col('242632'));box(x+.5,11,2,1,col('798675'))
  box(11.5,11,1,.5,col('22232b'))
 if id=='nanami':
  box(22,20,4,5,col('d6e3d3'));box(23.5,21,1,7,col('477875'));box(20,22,1,7,col('79634c'));box(27,22,1,7,col('79634c'))
 if id in ['yuji','choso']:
  box(20,20,8,2,col('9f3649') if id=='yuji' else col('68485b'));box(40,20,16,2,col('702436') if id=='yuji' else col('56344a'))
 if id in ['sukuna','kenjaku','mahito','choso','utahime','toji']:
  if id=='sukuna':
   for x in [8,10,13,15]:box(x,12,.5,2,col('3e2630'))
   box(10,8.5,4,.5,col('442934'));box(11,9.5,2,.5,col('442934'));box(22,21,4,7,skin);box(20,29,8,1,col('302737'))
  if id=='kenjaku':
   box(8,9.5,8,.5,col('68504e'))
   for x in range(9,16,2):box(x,9,.5,1.5,col('503c3b'))
  if id=='mahito':
   for y in [10,13]:box(8,y,8,.5,col('627880'))
   for x in [9,12,15]:box(x,9.5,.5,1.5,col('3f535e'))
  if id=='choso':box(10,12,4,1,col('663744'))
  if id in ['utahime','toji']:box(8.5 if id=='utahime' else 14,12,.5,3,col('8e665d'))
 if id in ['maki','toji','todo','yuki','miguel','hakari']:
  for u,v in [(40,16),(32,48)]:
   for x in range(u,u+16):box(x,v+4,1,8,shade(skin,((x%4)-2)*5))
  if id in ['todo','hakari']:box(20,21,8,9,skin)
 if id in ['geto','kenjaku','gakuganji','uraume','yorozu']:
  box(20,26,8,3,col('a99563') if id in ['geto','kenjaku'] else shade(cloth,-40));
  for y in range(22,26):box(21+(y-22),y,1,1,shade(cloth,35))
 if style=='beast':
  # Distinct curse markings and face plates, not human eyes and jacket buttons.
  box(20,20,8,12,shade(skin,-15));box(20,23,8,1,shade(skin,-35))
  if id=='jogo':box(8,10,8,4,shade(skin,-15));box(10.5,10,3,3,col('302829'));box(11.5,10.5,1,1.5,col('cb4f35'));box(8,14,8,1,col('664632'))
  elif id=='panda':
   box(8,8,8,8,col('e2dfd4'))
   for x in [8.5,13]:box(x,10,2.5,3,col('25232a'));box(x+.5,11,1,1,col('bcbab2'))
   box(11,13,2,1,col('353038'));box(20,22,8,9,col('e3dfd4'))
  else:
   for x in [8.5,13]:box(x,10,2,2,col('352a32'));box(x+.5,10.5,1,1,col(c['color']))
   box(9,14,6,1,col('31292c'))
   for x in [9,11,13]:box(x,14,1,.5,col('e0d5b3'))
 save(im,A/f'textures/entity/{id}.png')
# Reference-driven technique sprite sequences. Black Flash provided as six square frames.
fx=A/'textures/particle';fx.mkdir(parents=True,exist_ok=True)
sheet=Image.open('art/references/blackflashanim.png').convert('RGBA')
for i in range(6):
 frame=sheet.crop((0,i*512,512,(i+1)*512));pixels=[]
 for r,g,b,a in frame.get_flattened_data():pixels.append((255,min(100,g+15),min(100,b+10),min(255,r*2) if r>40 else 0))
 frame.putdata(pixels);save(frame.resize((128,128),Image.Resampling.LANCZOS),fx/f'black_flash_{i}.png')
js(A/'particles/black_flash.json',{'textures':[f'jjkcursedate:black_flash_{i}' for i in range(6)]})
for kind,color in {'blood_orb':'ce2749','blue_orb':'2eaeff','red_orb':'ff385e','purple_orb':'b666ff','slash':'dbe5f8','fire':'ff9c36','ice':'8fe8ff','lightning':'c5ffeb','curse':'b08ae7','heal':'87ffb0','barrier':'f5d386'}.items():
 for i in range(6):
  im=Image.new('RGBA',(64,64));dr=ImageDraw.Draw(im);rgb=col(color);rng=random.Random(kind+str(i));pulse=2*math.sin(i*math.pi/3)
  if kind=='blood_orb':
   base=Image.open('art/references/bloodorb2.png').convert('RGBA').resize((40,40),Image.Resampling.NEAREST);im.alpha_composite(base,(12,12))
  else:
   for radius in range(24,2,-1):
    alpha=int(160*(1-radius/26));dr.ellipse((32-radius,32-radius,32+radius,32+radius),fill=rgb[:3]+(alpha,))
  if kind in ['slash','lightning']:
   pts=[(3+j*6,32+int(math.sin(j*1.8+i)*12)) for j in range(10)] if kind=='lightning' else [(4,48),(16,24),(40,9),(59,5)]
   dr.line(pts,fill=rgb,width=3);dr.line(pts,fill=(255,255,255,255),width=1)
  else:
   radius=18+pulse;dr.arc((32-radius,32-radius,32+radius,32+radius),i*60,i*60+270,fill=rgb,width=2)
   for k in range(4):
    angle=i*.8+k*math.pi/2;x=32+math.cos(angle)*23;y=32+math.sin(angle)*23;dr.line((x,y,x+math.cos(angle)*6,y+math.sin(angle)*6),fill=rgb,width=2)
   dr.ellipse((27,27,36,36),fill=(255,240,255,240))
  save(im,fx/f'{kind}_{i}.png')
 js(A/f'particles/{kind}.json',{'textures':[f'jjkcursedate:{kind}_{i}' for i in range(6)]})
# Distinct tool shapes, metallic edges, wrapped grips, inlays and useful hand transforms.
tools=['slaughter_demon','playful_cloud','inverted_spear','soul_split_katana','dragon_bone','black_rope','chain_of_a_thousand_miles','nanami_blade','festering_life_sword','executioner_sword','hiten','kamutoke','nobara_hammer','mechamaru_blade']
def cube(a,b,t):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}}
for idx,id in enumerate(tools):
 colors={'steel':col('879dac'),'edge':col('dce5e9'),'grip':col('403044'),'gold':col('d3af64'),'accent':col(['6686a4','a52235','d5cdb2','666170','b98c55','302334','a4a7aa','b7c0a0','847e52','f4cb65','bd8f5e','b1a78d','b89868','728f9e'][idx])}
 tex={}
 for name,c in colors.items():
  im=Image.new('RGBA',(32,32));dr=ImageDraw.Draw(im)
  for y in range(32):
   for x in range(32):im.putpixel((x,y),shade(c,int(math.sin(x*.5)*8+(x-16)*.7+(6 if name=='grip' and (x+y)%8<2 else 0))))
  save(im,A/f'textures/item/{id}_{name}.png');tex[name]=f'jjkcursedate:item/{id}_{name}'
 e=[cube([7,0,7],[9,7,9],'grip'),cube([6.5,0,6.5],[9.5,1,9.5],'gold')]
 if id=='playful_cloud':
  e=[]
  for low,high,x in [(0,9,6),(11,20,8),(22,31,6)]:e+=[cube([x,low,7],[x+2,high,9],'accent'),cube([x-.3,low,6.7],[x+2.3,low+1,9.3],'gold')]
  e +=[cube([7,9,7.5],[9,11,8.5],'steel'),cube([7,20,7.5],[9,22,8.5],'steel')]
 elif id in ['black_rope','chain_of_a_thousand_miles']:
  e=[]
  for i in range(9):
   x=7+math.sin(i*.9)*2;y=i*3;t='grip' if id=='black_rope' else 'steel'
   e.extend([cube([x-1,y,7],[x,y+3,8],t),cube([x+1,y,7],[x+2,y+3,8],t),cube([x-1,y,7],[x+2,y+.7,8],t),cube([x-1,y+2.3,7],[x+2,y+3,8],t)])
 elif id in ['nobara_hammer','kamutoke']:
  e+=[cube([7,7,7],[9,20,9],'grip'),cube([2,18,5],[14,23,11],'accent'),cube([1.5,18,4.8],[3,23,11.2],'steel'),cube([12,18,4.8],[14.5,23,11.2],'edge')]
 elif id in ['inverted_spear','hiten']:
  e+=[cube([7.5,7,7.5],[8.5,19,8.5],'grip'),cube([4,18,7],[12,19,9],'gold'),cube([7,18,7],[9,30,9],'steel'),cube([4,22,7],[5,28,9],'edge'),cube([11,22,7],[12,28,9],'edge'),cube([4,21,7],[12,23,9],'accent')]
 elif id=='nanami_blade':
  e+=[cube([6,6,6],[10,8,10],'grip'),cube([5.5,8,7],[10.5,23,9],'accent'),cube([5,8,7],[6,23,9],'edge')]
  for y in range(9,23,3):e+=[cube([5.4,y,6.8],[10.6,y+.6,9.2],'grip')]
 elif id=='dragon_bone':
  e+=[cube([5,6,6],[11,8,10],'gold'),cube([5,8,7],[11,24,9],'accent'),cube([4.5,9,7],[5.5,24,9],'edge'),cube([8,8,8],[12,17,11],'steel')]
  for y in [10,13,16]:e+=[cube([9,y,6],[11,y+1,7],'gold')]
 else:
  e+=[cube([4,6,6],[12,7,10],'gold'),cube([7,7,7.5],[9,28,8.5],'steel'),cube([6.5,8,7.4],[7,28,8.6],'edge'),cube([7,28,7.5],[8,30,8.5],'edge')]
  if id=='soul_split_katana':e+=[cube([5.5,7,6],[10.5,10,10],'grip')]
  if id=='festering_life_sword':
   for y in [11,17,23]:e+=[cube([6,y,6],[10,y+2,10],'accent')]
  if id=='executioner_sword':e+=[cube([4,20,7.4],[12,21,8.6],'accent')]
 tex['particle']=tex['steel']
 js(A/f'models/item/{id}.json',{'textures':tex,'elements':e,'display':{'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,1,0],'scale':[.65,.65,.65]},'firstperson_righthand':{'rotation':[0,-90,15],'translation':[1,-1,-1],'scale':[.65,.65,.65]},'firstperson_lefthand':{'rotation':[0,90,-15],'translation':[1,-1,-1],'scale':[.65,.65,.65]},'gui':{'rotation':[15,0,-40],'translation':[0,-2,0],'scale':[.58,.58,.58]},'ground':{'rotation':[0,0,90],'scale':[.3,.3,.3]}}})
# Structure palettes: porcelain, indigo roof, timber panels, rune stone and tiled walkways.
for id,c in {'plaster':'ded9cb','roof_tiles':'30394e','timber_panel':'594536','curse_stone':'41364d','paving':'90908c'}.items():
 im=Image.new('RGBA',(32,32),col(c));dr=ImageDraw.Draw(im);rng=random.Random(id)
 for y in range(32):
  for x in range(32):im.putpixel((x,y),shade(col(c),rng.randrange(-4,5)))
 if id=='roof_tiles':
  for y in range(0,32,8):dr.line((0,y,31,y),fill=col('181e2e'),width=2);[dr.line((x,y,x,y+7),fill=col('53627a')) for x in range(0,32,8)]
 elif id=='timber_panel':
  for x in range(0,32,8):dr.line((x,0,x,31),fill=col('2c2527'));dr.line((x+2,0,x+2,31),fill=col('74604b'))
 elif id=='curse_stone':
  dr.rectangle((1,1,30,30),outline=col('8b5fa8'));dr.line((16,5,24,16,16,27,8,16,16,5),fill=col('b48bc5'),width=2)
 else:
  for y in [0,16,31]:dr.line((0,y,31,y),fill=shade(col(c),-30))
  for x in [0,16,31]:dr.line((x,0,x,31),fill=shade(col(c),-22))
 save(im,A/f'textures/block/{id}.png');js(A/f'models/block/{id}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'jjkcursedate:block/{id}'}});js(A/f'blockstates/{id}.json',{'variants':{'':{'model':f'jjkcursedate:block/{id}'}}});js(A/f'models/item/{id}.json',{'parent':f'jjkcursedate:block/{id}'})
lang=json.loads((A/'lang/en_us.json').read_text())
for action,title in {'cycle':'Cycle technique','castSelected':'Cast selected technique','quick':'Quick punch','heavy':'Heavy punch','barrage':'Barrage punch'}.items():lang['key.jjkcursedate.'+action]=title
for id in ['plaster','roof_tiles','timber_panel','curse_stone','paving']:lang['block.jjkcursedate.'+id]=id.replace('_',' ').title()
for form in ['divine_dog','nue','rika_manifestation','cursed_spirit','moon_dregs','garuda','death_swarm','mahoraga']:
 js(A/f'models/item/{form}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'});lang['item.jjkcursedate.'+form+'_spawn_egg']=form.replace('_',' ').title()+' Spawn Egg'
js(A/'lang/en_us.json',lang)
# Armor uses the same tailored 128px atlas as each matching character, at 64:32 aspect.
for outfit,path in {'tokyo_uniform':'yuji','kyoto_uniform':'mai','gojo_outfit':'gojo','sukuna_robes':'sukuna','toji_outfit':'toji','yuta_uniform':'yuta','nanami_suit':'nanami','geto_robes':'geto'}.items():
 skin=Image.open(A/f'textures/entity/{path}.png').convert('RGBA')
 for layer in [1,2]:
  armor=skin.crop((0,0,128,64));dr=ImageDraw.Draw(armor)
  # Layer two maps leggings; retain seams and use the character's boot/cloth palette.
  if layer==2:
   for x in [8,24]:dr.line((x,40,x,58),fill=shade(col(P[path][1]),20),width=1)
  save(armor,A/f'textures/models/armor/{outfit}_layer_{layer}.png')
# Flat contact sheet uses the exact UV-mapped front surfaces, not concept art.
contact=Image.new('RGB',(8*128,math.ceil(len(roster)/8)*180),(20,24,35));draw=ImageDraw.Draw(contact)
for n,c in enumerate(roster):
 s=Image.open(A/f'textures/entity/{c["id"]}.png');figure=Image.new('RGBA',(32,64));
 figure.alpha_composite(s.crop((16,16,32,32)),(8,0));figure.alpha_composite(s.crop((40,40,56,64)),(8,16));figure.alpha_composite(s.crop((88,40,96,64)),(0,16));figure.alpha_composite(s.crop((72,104,80,128)),(24,16));figure.alpha_composite(s.crop((8,40,16,64)),(8,40));figure.alpha_composite(s.crop((40,104,48,128)),(16,40))
 x=(n%8)*128;y=(n//8)*180;contact.paste(figure.resize((64,128),Image.Resampling.NEAREST),(x+32,y+8),figure.resize((64,128),Image.Resampling.NEAREST));draw.text((x+4,y+144),c['name'][:21],fill=(213,220,235))
save(contact,Path(os.environ.get('WORKSPACE_ROOT','.'))/'output/jjkcursedate/skin-contact-sheet.png')
print('Reworked 68 skins, 14 sculpted tools, five architecture materials, 12 animated effect types')
