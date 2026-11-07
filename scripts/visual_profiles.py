"""Author explicit animation/effect directions for every roster path; no skin writes."""
import json
from pathlib import Path
root=Path('src/main/resources')
# id | cast gesture | the three ordered move silhouettes | domain scene motif
rows='''gojo|finger|blue red purple|void
yuji|brawler|impact black_flash soul_slash|none
sukuna|cleave|slash cleave flame|shrine
megumi|shadow|shadow lightning shadow_step|shadow
nobara|hammer|nails hairpin resonance|none
maki|blade|weapon rush soul_slash|none
toge|speech|speech speech speech|none
panda|brawler|impact rush shockwave|none
nanami|ratio|ratio fracture overtime|none
yuta|blade|summon speech love_beam|blades
geto|orb|summon swarm vortex|none
mahito|palms|transfigure shockwave summon|hands
jogo|palms|embers eruption meteor|volcano
hanami|ritual|roots wood flowers|none
dagon|tide|water swarm water|ocean
toji|blade|weapon rush chain|none
choso|blood|blood supernova blood_scale|none
todo|clap|clap impact rush|none
kamo|blood|blood_arrow blood_bind blood_scale|none
momo|sweep|wind flight wind|none
mai|aim|bullet metal bullet|none
miwa|draw|slash weapon barrier|none
mechamaru|aim|cannon volley armor|none
utahime|ritual|ritual ritual heal|none
shoko|palms|heal shield shockwave|none
gakuganji|sweep|sound sound sound|none
kusakabe|draw|slash barrier weapon|none
mei_mei|sweep|feather summon weapon|none
ui_ui|ritual|teleport barrier heal|none
naobito|frame|projection frame projection|none
naoya|frame|projection frame shockwave|panels
yuki|brawler|star summon gravity|none
tengen|ritual|barrier barrier barrier|none
kenjaku|orb|summon gravity vortex|womb
uraume|palms|ice frost icefall|none
haruta|blade|weapon heal impact|none
eso|sweep|poison poison blood|none
kechizu|brawler|poison impact poison|none
finger_bearer|orb|cannon impact shockwave|none
smallpox|ritual|grave poison grave|graves
grasshopper|brawler|rush impact wind|none
rika|claw|cleave love_beam shield|none
higuruma|gavel|gavel frame execution|court
hakari|brawler|jackpot doors rush|jackpot
kirara|star|star star constellation|none
kashimo|staff|lightning weapon lightning|none
hara|receipt|receipt barrier gravity|none
takaba|clap|comedy shockwave comedy|none
hanezoki|aim|embers flame heal|none
ryu|cannon|cannon shockwave volley|none
uro|tide|sky shatter sky|none
kurourushi|claw|poison swarm transfigure|none
dhruv|ritual|summon slash barrier|none
angel|prayer|angel ladder flight|none
yorozu|orb|metal armor perfect_sphere|sphere
daido|draw|slash weapon fracture|none
miyo|sumo|rush shockwave shield|sumo
larue|heart|heart heart heart|none
miguel|sweep|rope ritual rush|none
junpei|shadow|summon poison shield|none
ijichi|ritual|barrier teleport heal|none
nitta|palms|heal teleport shield|none
arata|palms|heal shield teleport|none
riko|prayer|shield teleport heal|none
kuro|blade|weapon teleport heal|none
tsumiki|prayer|shield teleport heal|none
wasuke|sumo|shield heal teleport|none
yuko|heart|shield heal teleport|none'''
roster=json.loads((root/'data/jjkcursedate/roster.json').read_text())
profiles={}
for n,row in enumerate(rows.splitlines()):
 id,gesture,moves,domain=row.split('|')
 # Small authored offsets distinguish people with the same training style.
 profiles[id]={'gesture':gesture,'moves':moves.split(),'domain':domain,
  'tempo':round(.85+(n%7)*.055,3),'guard':round(.12+(n%5)*.065,3),
  'lean':round(.06+(n%6)*.022,3),'spread':round(.15+(n%4)*.09,3),
  'lead':-1 if id in {'maki','toji','mai','miwa','kamo','uro','kirara','yuko'} else 1,
  'color':next(c['color'] for c in roster if c['id']==id)}
assert set(profiles)=={c['id'] for c in roster}
p=root/'data/jjkcursedate/visuals.json';p.write_text(json.dumps(profiles,indent=2)+'\n')
print(f'Authored {len(profiles)} character profiles / {sum(len(p["moves"]) for p in profiles.values())} move directions')
