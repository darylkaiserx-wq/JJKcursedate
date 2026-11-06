"""Content catalog and reproducible source of gameplay definitions."""
import json
from pathlib import Path
# id | name | role | palette | technique 1:kind | technique 2:kind | technique 3:kind | domain
ROWS='''gojo|Satoru Gojo|trainer|84dbff|Lapse Blue:pull|Reversal Red:push|Hollow Purple:blast|Unlimited Void
 yuji|Yuji Itadori|ally|f48da6|Divergent Fist:strike|Black Flash:rush|Soul Strike:slash|
 sukuna|Ryomen Sukuna|boss|e85164|Dismantle:slash|Cleave:strike|Divine Flame:fire|Malevolent Shrine
 megumi|Megumi Fushiguro|ally|59699e|Divine Dogs:summon|Nue:lightning|Shadow Step:teleport|Chimera Shadow Garden
 nobara|Nobara Kugisaki|ally|e69b58|Nail Shot:slash|Hairpin:blast|Resonance:curse|
 maki|Maki Zenin|ally|729b65|Heavenly Strike:strike|Weapon Rush:rush|Soul Split:slash|
 toge|Toge Inumaki|ally|bca5d9|Stop:stun|Blast Away:push|Sleep:curse|
 panda|Panda|ally|bcbcbc|Panda Punch:strike|Gorilla Core:rush|Drumming Beat:push|
 nanami|Kento Nanami|trainer|dcce85|Ratio Strike:strike|Collapse:blast|Overtime:shield|
 yuta|Yuta Okkotsu|ally|d7e7ef|Rika:summon|Cursed Speech:stun|Love Beam:blast|Authentic Mutual Love
 geto|Suguru Geto|boss|9a7cbd|Cursed Spirit:summon|Spirit Swarm:curse|Maximum Uzumaki:blast|
 mahito|Mahito|boss|8dc6ce|Soul Transfiguration:curse|Body Repel:push|Polymorphic Soul:summon|Self Embodiment of Perfection
 jogo|Jogo|boss|ff803f|Ember Insects:fire|Volcanic Burst:blast|Maximum Meteor:fire|Coffin of the Iron Mountain
 hanami|Hanami|boss|91be77|Cursed Roots:plant|Wooden Ball:blast|Flower Field:stun|
 dagon|Dagon|boss|df7779|Tidal Wave:water|Death Swarm:summon|Ocean Pressure:push|Horizon of the Captivating Skandha
 toji|Toji Fushiguro|boss|728392|Inverted Spear:strike|Heavenly Dash:rush|Chain of a Thousand Miles:pull|
 choso|Choso|ally|c15c7d|Piercing Blood:blood|Supernova:blast|Flowing Red Scale:shield|
 todo|Aoi Todo|trainer|bc985c|Boogie Woogie:swap|Heavy Clap:strike|Brotherly Rush:rush|
 kamo|Noritoshi Kamo|ally|b86574|Blood Arrow:blood|Crimson Binding:stun|Flowing Red Scale:shield|
 momo|Momo Nishimiya|ally|e3d899|Wind Scythe:slash|Broom Flight:flight|Wind Gust:push|
 mai|Mai Zenin|ally|658b80|Cursed Bullet:slash|Construction:shield|Last Bullet:blast|
 miwa|Kasumi Miwa|ally|91b6e8|Batto Sword Drawing:slash|New Shadow Style:strike|Simple Domain:shield|
 mechamaru|Kokichi Muta|ally|bda082|Ultra Cannon:blast|Puppet Volley:slash|Absolute Mode:shield|
 utahime|Utahime Iori|trainer|db99ae|Solo Forbidden Area:shield|Ritual Pulse:push|Support Chant:heal|
 shoko|Shoko Ieiri|trainer|b38d77|Reverse Healing:heal|Medical Reinforcement:shield|Cursed Pulse:push|
 gakuganji|Yoshinobu Gakuganji|trainer|b1a287|Guitar Wave:push|Amplified Chord:blast|Resonant Burst:stun|
 kusakabe|Atsuya Kusakabe|trainer|9a9279|Evening Moon:slash|Simple Domain:shield|Sword Interception:strike|
 mei_mei|Mei Mei|trainer|b3bfd9|Bird Strike:slash|Black Bird:summon|Battle Axe:strike|
 ui_ui|Ui Ui|ally|a0c8e3|Spatial Transfer:teleport|Simple Domain:shield|Evacuation:heal|
 naobito|Naobito Zenin|trainer|b78c72|Projection Rush:rush|Frame Lock:stun|Twenty Four Frames:shield|
 naoya|Naoya Zenin|boss|bfbd91|Projection Dash:rush|Frame Freeze:stun|Mach Strike:blast|Time Cell Moon Palace
 yuki|Yuki Tsukumo|ally|e2c866|Star Rage:strike|Garuda:summon|Mass Impact:blast|
 tengen|Master Tengen|trainer|c8bd9b|Barrier Art:shield|Spatial Barrier:push|Pure Barrier:stun|
 kenjaku|Kenjaku|boss|af88be|Spirit Manipulation:summon|Antigravity:gravity|Uzumaki:blast|Womb Profusion
 uraume|Uraume|boss|9cdeed|Ice Formation:ice|Frost Calm:stun|Icefall:blast|
 haruta|Haruta Shigemo|boss|dcc969|Hand Sword:slash|Miracle:heal|Lucky Strike:strike|
 eso|Eso|boss|d68bba|Wing King:poison|Decay:curse|Blood Spray:blood|
 kechizu|Kechizu|boss|8eae87|Rot Blood:poison|Cursed Bite:strike|Decay Mist:curse|
 finger_bearer|Finger Bearer|boss|d8c4a2|Cursed Volley:blast|Brutal Claw:strike|Energy Burst:push|
 smallpox|Smallpox Deity|boss|959f70|Grave Binding:stun|Disease:poison|Burial:gravity|Smallpox Graveyard
 grasshopper|Grasshopper Curse|enemy|8ead63|Leaping Kick:rush|Mandible:strike|Wing Gust:push|
 rika|Rika Orimoto|ally|d4badf|Queen Claw:strike|Love Blast:blast|Protective Rage:shield|
 higuruma|Hiromi Higuruma|ally|a39c8b|Gavel:strike|Confiscation:stun|Executioners Sword:slash|Deadly Sentencing
 hakari|Kinji Hakari|ally|b6c37a|Reserve Ball:blast|Shutter Doors:stun|Jackpot Rush:rush|Idle Death Gamble
 kirara|Kirara Hoshi|ally|c8a1dd|Love Rendezvous:pull|Star Repulsion:push|Southern Cross:stun|
 kashimo|Hajime Kashimo|boss|98dfd4|Lightning Discharge:lightning|Staff Strike:strike|Mythical Beast Amber:rush|
 hara|Reggie Star|boss|dbbf8c|Contractual Recreation:blast|Receipt Shield:shield|Recreated Weight:gravity|
 takaba|Fumihiko Takaba|ally|f1db65|Comedian:heal|Slapstick:push|Punchline:stun|
 hanezoki|Iori Hazenoki|boss|d5a28d|Explosive Tooth:blast|Explosive Eye:fire|Reverse Recovery:heal|
 ryu|Ryu Ishigori|boss|b78cd0|Granite Blast:blast|Maximum Output:push|Cursed Barrage:slash|
 uro|Takako Uro|boss|a8d7e9|Sky Manipulation:shield|Thin Ice Breaker:blast|Sky Fold:swap|
 kurourushi|Kurourushi|boss|a19765|Festering Blade:poison|Cockroach Swarm:summon|Life Drain:curse|
 dhruv|Dhruv Lakdawalla|boss|c8a786|Shikigami Orbit:summon|Territory Slash:slash|Orbital Barrier:shield|
 angel|Hana Kurusu|ally|e9dba0|Technique Extinguishment:stun|Jacobs Ladder:blast|Angelic Flight:flight|
 yorozu|Yorozu|boss|9bb9a8|Liquid Metal:slash|Insect Armor:shield|Perfect Sphere:blast|Threefold Affliction
 daido|Hagane Daido|ally|c4bdac|Katana Mastery:slash|Perception:strike|Cut Everything:blast|
 miyo|Rokujushi Miyo|ally|b7a184|Sumo Charge:rush|Ring Toss:push|Sumo Training:shield|Sumo Ring
 larue|Larue|ally|c4a0a7|Heart Catch:pull|Attention Grab:stun|Heart Grip:strike|
 miguel|Miguel|ally|c99562|Black Rope:strike|Prayer Song:shield|Rhythm Step:rush|
 junpei|Junpei Yoshino|ally|9dbbc4|Moon Dregs:summon|Jellyfish Venom:poison|Poison Guard:shield|
 ijichi|Kiyotaka Ijichi|civilian|a5b0bb|Curtain:shield|Retreat:teleport|First Aid:heal|
 nitta|Akari Nitta|civilian|e8bc9f|First Aid:heal|Retreat:teleport|Support:shield|
 arata|Arata Nitta|civilian|dbc399|Painkiller:heal|Stabilize:shield|Retreat:teleport|
 riko|Riko Amanai|civilian|80c9e2|Encouragement:shield|Retreat:teleport|First Aid:heal|
 kuro|Misato Kuroi|civilian|b19ca1|Guard:strike|Retreat:teleport|First Aid:heal|
 tsumiki|Tsumiki Fushiguro|civilian|d8b9ac|Encouragement:shield|Retreat:teleport|First Aid:heal|
 wasuke|Wasuke Itadori|civilian|b3b3ac|Resolve:shield|Encouragement:heal|Retreat:teleport|
 yuko|Yuko Ozawa|civilian|bfa2b2|Encouragement:shield|First Aid:heal|Retreat:teleport|'''
roster=[]
for row in ROWS.strip().splitlines():
    ident,name,role,color,*rest=row.strip().split('|')
    moves=[]
    for i,entry in enumerate(rest[:3]):
        title,kind=entry.split(':')
        moves.append(dict(name=title,kind=kind,cost=[14,26,44][i],cooldown=[35,90,180][i],level=[1,8,18][i],power=[6,10,17][i]))
    if ident in ['maki','toji']:
        for move in moves:move['cost']=0
    roster.append(dict(id=ident,name=name,role=role,color=color,moves=moves,domain=rest[3]))
root=Path('src/main/resources')
(root/'data/jjkcursedate/roster.json').write_text(json.dumps(roster,indent=2)+'\n')
print(f'{len(roster)} characters; {len(roster)*3} move definitions; {sum(bool(c["domain"]) for c in roster)} domains')
