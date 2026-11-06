"""Generate original pixel art, extruded tool models, and worldgen data without image dependencies."""
import json,struct,zlib,math,random
from pathlib import Path
ROOT=Path('src/main/resources'); A=ROOT/'assets/jjkcursedate'; D=ROOT/'data/jjkcursedate'
roster=json.loads((D/'roster.json').read_text());lang={}
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n')
def png(path,w,h,pixels):
    def chunk(t,b):return struct.pack('>I',len(b))+t+b+struct.pack('>I',zlib.crc32(t+b)&0xffffffff)
    raw=b''.join(b'\0'+bytes(v for rgba in row for v in rgba) for row in pixels)
    path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
def image(w,h,color=(0,0,0,0)):return [[color for _ in range(w)] for _ in range(h)]
def rect(p,x,y,w,h,c):
    for yy in range(y,min(y+h,len(p))):
        for xx in range(x,min(x+w,len(p[0]))):p[yy][xx]=tuple(c)
def shade(c,n):return tuple(max(0,min(255,v+n)) for v in c[:3])+(255,)
for c in roster:
    ident=c['id']; rgb=tuple(bytes.fromhex(c['color']))+(255,);p=image(64,64)
    skin=(207,166,139,255);hair=(38,32,36,255);cloth=(28,33,53,255)
    if ident in ['gojo','toge','uraume']:hair=(230,236,242,255)
    if ident in ['yuji','sukuna']:hair=(220,128,145,255)
    if ident in ['nobara','mai','yuko']:hair=(136,77,39,255)
    if ident in ['nanami','yuki','momo','naoya','haruta']:hair=(214,190,109,255)
    if ident in ['miwa','mahito']:hair=(133,172,194,255)
    if ident in ['maki','megumi']:hair=(28,58,47,255)
    if ident in ['jogo','hanami','dagon','finger_bearer','grasshopper','smallpox','kurourushi','kechizu','rika']:skin=rgb
    if c['role']=='civilian':cloth=shade(rgb,-35)
    if ident in ['yuta','sukuna','uraume']:cloth=(225,219,211,255)
    # Head, limbs and body UV islands with per-face shading.
    rect(p,0,0,32,16,skin);rect(p,0,0,32,8,hair);rect(p,0,8,8,8,hair);rect(p,16,8,16,8,hair)
    rect(p,8,8,8,2,hair);rect(p,9,11,2,1,(36,35,43,255));rect(p,13,11,2,1,(36,35,43,255));rect(p,11,14,2,1,shade(skin,-36))
    if ident=='gojo':rect(p,8,10,8,3,(18,20,28,255))
    if ident=='sukuna':
        for x in [8,10,13,15]:rect(p,x,13,1,2,(67,33,36,255))
    if ident=='panda':
        rect(p,0,0,32,16,(228,228,220,255));rect(p,8,10,3,3,(29,29,34,255));rect(p,13,10,3,3,(29,29,34,255))
    for x,y,w,h in [(16,16,24,16),(40,16,16,16),(32,48,16,16)]:rect(p,x,y,w,h,cloth)
    for x,y in [(0,16),(16,48)]:rect(p,x,y,16,16,shade(cloth,-10));rect(p,x,y+12,16,4,(33,29,30,255))
    rect(p,44,28,4,4,skin);rect(p,36,60,4,4,skin)
    rect(p,20,20,8,1,rgb);rect(p,23,21,1,9,shade(cloth,22));
    for y in [22,25,28]:rect(p,24,y,1,1,(207,182,106,255))
    # Subtle fabric texture on opaque body pixels.
    rnd=random.Random(ident)
    for y in range(16,64):
        for x in range(64):
            if p[y][x][3] and rnd.random()<.09:p[y][x]=shade(p[y][x],rnd.choice([-6,6]))
    png(A/f'textures/entity/{ident}.png',64,64,p)
    write(A/f'models/item/{ident}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
    lang[f'entity.jjkcursedate.{ident}']=c['name'];lang[f'item.jjkcursedate.{ident}_spawn_egg']=c['name']+' Spawn Egg'
tools=['slaughter_demon','playful_cloud','inverted_spear','soul_split_katana','dragon_bone','black_rope','chain_of_a_thousand_miles','nanami_blade','festering_life_sword','executioner_sword','hiten','kamutoke','nobara_hammer','mechamaru_blade']
def cube(a,b,tex='#metal'):
    return {'from':a,'to':b,'faces':{f:{'texture':tex,'uv':[0,0,16,16]} for f in ['north','south','east','west','up','down']}}
metal=image(16,16,(145,167,183,255))
for y in range(16):
    for x in range(16):metal[y][x]=shade((140,160,180,255),(x+y)%5*8)
png(A/'textures/item/forged_metal.png',16,16,metal)
wrap=image(16,16,(61,41,56,255))
for y in range(16):rect(wrap,0,y,16,1,(98,57,73,255) if y%4<2 else (40,29,42,255))
png(A/'textures/item/wrapped_hilt.png',16,16,wrap)
for i,ident in enumerate(tools):
    elements=[cube([7,0,7],[9,7,9],'#wrap'),cube([4,6,6],[12,7,10]),cube([7,7,7],[9,24,9])]
    if ident in ['nobara_hammer','kamutoke']:elements=[cube([7,0,7],[9,19,9],'#wrap'),cube([2,17,5],[14,23,11])]
    elif ident=='playful_cloud':elements=[cube([7,-5,7],[9,28,9],'#wrap'),cube([6,6,6],[10,9,10]),cube([6,18,6],[10,21,10])]
    elif ident in ['black_rope','chain_of_a_thousand_miles']:elements=[cube([6,0,7],[8,9,9],'#wrap'),cube([8,7,7],[10,16,9],'#wrap'),cube([6,14,7],[8,24,9],'#wrap')]
    elif ident in ['inverted_spear','hiten']:elements+=[cube([4,19,7],[6,25,9]),cube([10,19,7],[12,25,9])]
    write(A/f'models/item/{ident}.json',{'textures':{'metal':'jjkcursedate:item/forged_metal','wrap':'jjkcursedate:item/wrapped_hilt','particle':'jjkcursedate:item/forged_metal'},'elements':elements,'display':{'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,2,0],'scale':[.7,.7,.7]},'firstperson_righthand':{'rotation':[0,-90,20],'translation':[1,2,0],'scale':[.6,.6,.6]},'gui':{'rotation':[20,0,-35],'translation':[0,-2,0],'scale':[.65,.65,.65]},'ground':{'scale':[.3,.3,.3]}}})
    lang['item.jjkcursedate.'+ident]=ident.replace('_',' ').title()
    ingredient=['minecraft:amethyst_shard','minecraft:emerald','minecraft:quartz','minecraft:obsidian','minecraft:redstone','minecraft:string','minecraft:chain','minecraft:lapis_lazuli','minecraft:spider_eye','minecraft:gold_ingot','minecraft:feather','minecraft:copper_ingot','minecraft:flint','minecraft:iron_ingot'][i]
    write(D/f'recipes/{ident}.json',{'type':'minecraft:crafting_shaped','pattern':[' F ',' M ',' S '],'key':{'F':{'item':'jjkcursedate:curse_fragment'},'M':{'item':ingredient},'S':{'item':'minecraft:diamond_sword'}},'result':{'item':'jjkcursedate:'+ident}})
for ident,color in {'tokyo_uniform':(29,33,60,255),'kyoto_uniform':(37,42,63,255),'gojo_outfit':(22,23,30,255),'sukuna_robes':(227,219,204,255),'toji_outfit':(31,45,39,255),'yuta_uniform':(232,230,218,255),'nanami_suit':(175,151,112,255),'geto_robes':(65,46,76,255)}.items():
    for layer in [1,2]:
        p=image(64,32,color)
        for y in range(32):
            for x in range(64):p[y][x]=shade(color,(x+y)%4-2)
        rect(p,20,20,8,2,(164,134,78,255));rect(p,23,20,1,12,shade(color,25));png(A/f'textures/models/armor/{ident}_layer_{layer}.png',64,32,p)
    for slot,vanilla in [('helmet','helmet'),('chestplate','chestplate'),('leggings','leggings'),('boots','boots')]:
        p=image(16,16);rect(p,3,3,10,10,color);rect(p,6,5,4,2,(202,171,94,255));png(A/f'textures/item/{ident}_{slot}.png',16,16,p)
        write(A/f'models/item/{ident}_{slot}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'jjkcursedate:item/{ident}_{slot}'}})
        lang[f'item.jjkcursedate.{ident}_{slot}']=ident.replace('_',' ').title()+' '+slot.title()
        write(D/f'recipes/{ident}_{slot}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:iron_'+vanilla},{'item':'jjkcursedate:curse_fragment'},{'item':'minecraft:'+['black','blue','purple','white','green','light_gray','brown','magenta'][list({'tokyo_uniform':0,'kyoto_uniform':1,'gojo_outfit':2,'sukuna_robes':3,'toji_outfit':4,'yuta_uniform':5,'nanami_suit':6,'geto_robes':7}).index(ident)]+'_dye'}],'result':{'item':f'jjkcursedate:{ident}_{slot}'}})
for ident in ['fate_book','curse_fragment']:
    p=image(16,16);rect(p,3,2,10,13,(76,49,94,255));rect(p,4,3,8,11,(32,39,58,255));rect(p,7,5,2,7,(216,189,113,255));rect(p,5,7,6,2,(216,189,113,255));png(A/f'textures/item/{ident}.png',16,16,p)
    write(A/f'models/item/{ident}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'jjkcursedate:item/{ident}'}})
    lang['item.jjkcursedate.'+ident]=ident.replace('_',' ').title()
lang['key.categories.jjkcursedate']='JJK Cursed Fate'
for action,title in {'cast1':'Technique I','cast2':'Technique II','cast3':'Technique III','domain':'Domain Expansion','rct':'Reverse Cursed Technique','guard':'Simple Domain','reinforce':'Cursed Reinforcement','journal':'Character Journal'}.items():lang['key.jjkcursedate.'+action]=title
write(A/'lang/en_us.json',lang)
write(D/'recipes/fate_book.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:book'},{'item':'minecraft:amethyst_shard'}],'result':{'item':'jjkcursedate:fate_book'}})
write(D/'worldgen/configured_feature/city_site.json',{'type':'jjkcursedate:city_site','config':{}})
write(D/'worldgen/placed_feature/city_site.json',{'feature':'jjkcursedate:city_site','placement':[{'type':'minecraft:rarity_filter','chance':192},{'type':'minecraft:heightmap','heightmap':'WORLD_SURFACE_WG'},{'type':'minecraft:biome'}]})
write(D/'forge/biome_modifier/sites.json',{'type':'forge:add_features','biomes':'#minecraft:is_overworld','features':['jjkcursedate:city_site'],'step':'surface_structures'})
write(D/'forge/biome_modifier/curses.json',{'type':'forge:add_spawns','biomes':'#minecraft:is_overworld','spawners':[{'type':'jjkcursedate:grasshopper','weight':8,'minCount':1,'maxCount':1},{'type':'jjkcursedate:finger_bearer','weight':2,'minCount':1,'maxCount':1}]})
# One empty 5x5x5 test template in standard gzip NBT.
import gzip
b=bytearray(b'\x0a\x00\x00')
def name(t,n):
    enc=n.encode();return bytes([t])+struct.pack('>H',len(enc))+enc
b+=name(3,'DataVersion')+struct.pack('>i',3465)
b+=name(9,'size')+bytes([3])+struct.pack('>i',3)+struct.pack('>iii',5,5,5)
b+=name(9,'palette')+bytes([10])+struct.pack('>i',1)+name(8,'Name')+struct.pack('>H',13)+b'minecraft:air'+b'\0'
b+=name(9,'blocks')+bytes([10])+struct.pack('>i',0)
b+=name(9,'entities')+bytes([10])+struct.pack('>i',0)+b'\0'
(D/'structures').mkdir(exist_ok=True);(D/'structures/empty.nbt').write_bytes(gzip.compress(b))
print('Generated textures, models, recipes, world generation, translations and test template')

for form,color in {'divine_dog':(48,50,64,255),'nue':(128,80,64,255),'rika_manifestation':(222,213,198,255),'cursed_spirit':(131,90,160,255),'moon_dregs':(137,196,215,255),'garuda':(178,191,139,255),'death_swarm':(115,163,187,255),'mahoraga':(203,192,158,255)}.items():
    p=image(64,64,color)
    for y in range(64):
        for x in range(64):p[y][x]=shade(color,((x*7+y*11)%9)-4)
    rect(p,9,9,2,2,(233,65,92,255));rect(p,13,9,2,2,(233,65,92,255));rect(p,10,12,5,1,(44,31,41,255))
    if form=='mahoraga':rect(p,48,0,16,8,(197,156,55,255))
    png(A/f'textures/entity/summon_{form}.png',64,64,p)
    lang['entity.jjkcursedate.'+form]=form.replace('_',' ').title()
lang['subtitles.jjkcursedate.impact']='Cursed energy strikes'
lang['subtitles.jjkcursedate.domain']='A domain expands'
write(A/'lang/en_us.json',lang)

(D/'structures/city_test.nbt').write_bytes(gzip.compress(bytes(b).replace(struct.pack('>iii',5,5,5),struct.pack('>iii',20,20,20))))
