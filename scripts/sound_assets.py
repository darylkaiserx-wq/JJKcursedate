"""Generate original short mono Vorbis effects. Requires Python 3 and ffmpeg."""
import json, math, random, struct, subprocess, tempfile, wave
from pathlib import Path
A=Path('src/main/resources/assets/jjkcursedate');out=A/'sounds';out.mkdir(exist_ok=True)
profiles={'quick':(.16,120,35),'heavy':(.35,85,22),'barrage':(.12,160,60),'charge':(.7,180,640),'black_flash':(.6,50,20),'slash':(.24,800,220),'fire':(.6,150,55),'lightning':(.4,900,90),'blood':(.35,220,95)}
for name,(duration,start,end) in profiles.items():
 rng=random.Random(name);samples=[];low=0
 for i in range(int(duration*22050)):
  t=i/22050;f=t/duration;noise=rng.uniform(-1,1);low=low*.82+noise*.18
  env=min(1,t/.009)*math.exp(-f*(2 if name=='charge' else 6))
  sweep=math.sin(2*math.pi*(start*t+(end-start)*t*t/(2*duration)))
  body=sweep*.45+low*.6+noise*(.18 if name in ['lightning','slash','black_flash','fire'] else .04)
  if name=='charge':env=math.sin(math.pi*f)**.6;body+=math.sin(t*2*math.pi*60)*.15
  if name=='black_flash':body+=noise*.35*max(0,1-abs(f-.18)*15)
  samples.append(int(max(-1,min(1,body*env*.8))*32767))
 with tempfile.TemporaryDirectory() as td:
  w=Path(td)/'sound.wav'
  with wave.open(str(w),'wb') as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(22050);f.writeframes(struct.pack('<'+'h'*len(samples),*samples))
  subprocess.run(['ffmpeg','-v','error','-y','-i',str(w),'-c:a','libvorbis','-q:a','4','-metadata','comment=CodeRabbit generated original synthesized effect; sound_assets.py',str(out/f'{name}.ogg')],check=True)
p=A/'sounds.json';sounds=json.loads(p.read_text());langp=A/'lang/en_us.json';lang=json.loads(langp.read_text())
for name in profiles:
 sounds[name]={'sounds':['jjkcursedate:'+name],'subtitle':'subtitles.jjkcursedate.'+name}
 lang['subtitles.jjkcursedate.'+name]={'quick':'Quick punch','heavy':'Heavy impact','barrage':'Punch barrage','charge':'Cursed energy surges','black_flash':'Black Flash cracks','slash':'Blade whooshes','fire':'Flames roar','lightning':'Lightning crackles','blood':'Blood technique pulses'}[name]
p.write_text(json.dumps(sounds,indent=2)+'\n');langp.write_text(json.dumps(lang,indent=2)+'\n')
print('Generated 9 original mono Vorbis combat effects')
