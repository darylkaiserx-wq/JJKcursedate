package dev.cursedfate;

import com.mojang.blaze3d.vertex.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Bounded client-only solid/ribbon geometry, driven exclusively by server casts. */
@Mod.EventBusSubscriber(modid=CursedFate.ID,value=Dist.CLIENT)
public final class TechniqueVisuals {
    private record Effect(Net.Technique data,long start,long until){}
    private static final List<Effect> ACTIVE=new ArrayList<>();
    private static net.minecraft.client.multiplayer.ClientLevel lastLevel;
    private static final Vec3 UP=new Vec3(0,1,0),FORWARD=new Vec3(0,0,1);
    public static void clear(){ACTIVE.clear();lastLevel=null;}
    public static void accept(Net.Technique data){
        var level=Minecraft.getInstance().level;
        if(level==null||!VisualCatalog.ALL.containsKey(data.path())||data.slot() < -1||data.slot()>2)return;
        if(!Double.isFinite(data.origin().lengthSqr())||!Double.isFinite(data.end().lengthSqr())||!Float.isFinite(data.radius()))return;
        if(lastLevel!=level){ACTIVE.clear();lastLevel=level;}
        long now=level.getGameTime(),start=now;
        if(data.slot()==-1){
            for(var old:ACTIVE)if(old.data().slot()==-1&&old.data().actor().equals(data.actor())){start=old.start();break;}
            ACTIVE.removeIf(e->e.data().slot()==-1&&e.data().actor().equals(data.actor()));
        }
        if(data.ticks()<=0)return;
        int cap=data.slot()==-1?240:50;
        if(ACTIVE.size()>=40)ACTIVE.remove(0);
        ACTIVE.add(new Effect(data,start,now+Mth.clamp(data.ticks(),1,cap)));
    }
    public static void tick(){var level=Minecraft.getInstance().level;if(level==null){clear();return;}if(lastLevel!=level){ACTIVE.clear();lastLevel=level;}ACTIVE.removeIf(e->level.getGameTime()>=e.until());}
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||Settings.PARTICLES.get()==0||ACTIVE.isEmpty())return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        var camera=event.getCamera().getPosition();var poses=event.getPoseStack();poses.pushPose();poses.translate(-camera.x,-camera.y,-camera.z);
        var buffers=mc.renderBuffers().bufferSource();var out=buffers.getBuffer(RenderType.lightning());
        var mesh=new Mesh(poses.last().pose(),out);
        for(var effect:ACTIVE){
            var data=effect.data();if(data.origin().distanceToSqr(camera)>14400)continue;
            float age=mc.level.getGameTime()-effect.start()+event.getPartialTick();
            float fade=Mth.clamp((effect.until()-mc.level.getGameTime()-event.getPartialTick())/8,0,1);
            if(data.slot()==-1)domain(mesh,data,age,fade);
            else technique(mesh,data,age,fade);
        }
        buffers.endBatch(RenderType.lightning());poses.popPose();
    }
    private static void technique(Mesh m,Net.Technique d,float age,float fade){
        var profile=VisualCatalog.get(d.path());String form=profile.moves().get(d.slot());
        int color=profile.tint();Vec3 start=d.origin(),end=d.end(),axis=end.subtract(start).normalize();if(axis.lengthSqr()<.1)axis=FORWARD;
        float t=Mth.clamp(age/36,0,1),release=Mth.clamp((age-5)/9,0,1),alpha=fade*.65f;
        Vec3 hand=start.add(axis.scale(1.2)).add(0,-.22,0),impact=start.lerp(end,release);
        double r=.3+Math.sin(Math.min(1,age/12)*Math.PI/2)*.6;
        switch(form){
            case "blue","red","purple","perfect_sphere"->{
                int c=form.equals("blue")?0x26baff:form.equals("red")?0xff243d:form.equals("purple")?0xa55aff:0x222238;
                Vec3 center=form.equals("purple")?hand.lerp(end,release):hand.add(axis.scale(form.equals("blue")?2:0));
                double size=form.equals("purple")?r*1.4:r;
                m.orb(center,size,c,alpha*.45f);m.orb(center,size*.28,0xffffff,alpha);
                for(int i=0;i<3;i++)m.ring(center,i==0?axis:i==1?UP:new Vec3(1,0,0),size*(1.1+i*.17),.045,age*.13+i,c,alpha);
                if(form.equals("purple")){m.orb(hand.add(-.55,0,0),.2*(1-release),0x32baff,alpha);m.orb(hand.add(.55,0,0),.2*(1-release),0xff284e,alpha);m.beam(hand,center,.12,c,alpha*.4f);}
                else for(int i=0;i<10;i++){double a=i*Math.PI/5+age*.11;Vec3 p=center.add(Math.cos(a)*2.6,Math.sin(a*2)*.5,Math.sin(a)*2.6);m.beam(form.equals("blue")?p:center,form.equals("blue")?p.lerp(center,release):p,.025,c,alpha*.5f);}
            }
            case "blood","blood_arrow","blood_bind","supernova","blood_scale"->{
                int c=0xb70b2e;
                if(form.equals("blood")||form.equals("blood_arrow")){m.beam(hand,impact,.065,c,alpha);m.beam(hand,impact,.018,0xffd1d4,alpha);m.ring(hand,axis,.3,.05,age*.2,c,alpha);if(release==1)m.star(end,axis,.55,.035,c,alpha);}
                else if(form.equals("supernova")){for(int i=0;i<9;i++){double a=i*Math.PI*2/9;Vec3 p=hand.add(Math.cos(a)*(1+release*2),Math.sin(a)*1.3,Math.sin(a)*2);m.orb(p,.15,c,alpha);m.beam(p,p.add(p.subtract(hand).normalize().scale(release*2)),.035,c,alpha);}}
                else if(form.equals("blood_bind")){for(int i=0;i<4;i++)m.ring(end.add(0,i*.25-.5,0),UP,.65,.04,age*.08,c,alpha);}
                else {for(int i=0;i<4;i++)m.ring(start.add(0,-i*.35,0),UP,.5,.055,age*.1,c,alpha);}
            }
            case "slash","cleave","soul_slash","weapon","execution","ratio","fracture","wood"->{
                int c=form.equals("soul_slash")?0x95f8ef:form.equals("execution")?0xffd133:form.equals("wood")?0x9f7655:0xd9efff;
                Vec3 at=start.lerp(end,Math.min(1,release*1.4));
                for(int i=0;i<(form.equals("cleave")?5:form.equals("fracture")?4:1);i++)m.arc(at.add(0,i*.22-.3,0),axis,1.2+i*.1,.08,age*.12+i*.7,c,alpha);
                if(form.equals("ratio")){Vec3 left=end.add(-1.1,0,0),right=end.add(1.1,0,0);m.beam(left,right,.025,0xffd276,alpha);for(int i=0;i<=10;i++){Vec3 p=left.lerp(right,i/10d);m.beam(p.add(0,-.12,0),p.add(0,.12,0),i==7?.045:.012,0xffd276,alpha);}}
            }
            case "black_flash","impact","rush","overtime","shield","armor"->{
                int c=form.equals("black_flash")?0xff092e:form.equals("overtime")?0xffc96b:color;
                Vec3 at=form.equals("rush")?impact:form.equals("shield")||form.equals("armor")||form.equals("overtime")?start.add(0,-.6,0):hand.add(axis.scale(release*1.6));
                m.ring(at,axis,.3+release*1.7,.05,age*.1,c,alpha);m.star(at,axis,.7+release,.025,c,alpha);
                if(form.equals("black_flash"))for(int i=0;i<8;i++)m.bolt(at,at.add(Math.sin(i*2)*2,Math.cos(i*2)*1.5,Math.sin(i*3)),age+i,c,alpha);
                if(form.equals("rush"))m.beam(start,impact,.035,c,alpha*.5f);
                if(form.equals("armor"))for(int i=0;i<6;i++)m.box(at.add(Math.sin(i)*.5,(i%3)*.4-.4,Math.cos(i)*.5),.26,.18,.12,c,alpha*.5f);
            }
            case "flame","embers","eruption","meteor"->{
                Vec3 at=form.equals("meteor")?end.add(0,(1-release)*8,0):impact;
                if(form.equals("meteor")){m.orb(at,1.7,0xd14919,alpha*.8f);m.orb(at,1.35,0xffb43a,alpha*.6f);m.beam(at,at.add(0,4,0),.65,0xf57323,alpha*.35f);}
                else for(int i=0;i<7;i++){double a=i*.9+age*.12;Vec3 p=at.add(Math.cos(a)*r*.6,i*.16-.5,Math.sin(a)*r*.6);m.crystal(p,.18,.7+(i%3)*.2,0xff7428,alpha);m.crystal(p,.08,.5,0xffe59a,alpha);}
                if(form.equals("eruption"))for(int i=0;i<8;i++){double a=i*Math.PI/4;m.crystal(end.add(Math.cos(a)*1.3,-.8,Math.sin(a)*1.3),.35,release*2.4,0xff6324,alpha);}
            }
            case "lightning","cannon","volley","love_beam","ladder","angel","bullet","nails","hairpin"->{
                int c=form.equals("lightning")?0x9cfff1:form.equals("love_beam")?0xffa7ee:form.equals("ladder")||form.equals("angel")?0xfff2b2:form.equals("nails")||form.equals("hairpin")?0xbccbd5:color;
                if(form.equals("lightning")){for(int i=0;i<3;i++)m.bolt(hand.add(0,i*.1,0),impact,age*.7+i,c,alpha);}
                else if(form.equals("ladder")||form.equals("angel")){m.beam(end.add(0,-1,0),end.add(0,10,0),.45,c,alpha*.35f);for(int i=0;i<4;i++)m.ring(end.add(0,i*2,0),UP,1.2-i*.13,.07,age*.06,c,alpha);m.beam(end.add(-1,3,0),end.add(1,3,0),.08,c,alpha);}
                else if(form.equals("nails")||form.equals("hairpin")||form.equals("bullet")){for(int i=0;i<(form.equals("bullet")?1:4);i++){Vec3 p=impact.add((i-1.5)*.2,0,0);m.beam(p.subtract(axis.scale(.55)),p,.035,c,alpha);if(!form.equals("bullet"))m.beam(p.add(-.08,0,0),p.add(.08,0,0),.03,c,alpha);if(form.equals("hairpin")&&release==1)m.star(p,axis,.7,.03,0xff619a,alpha);}}
                else {double width=form.equals("volley")?.05:form.equals("love_beam")?.32:.22;m.beam(hand,impact,width,c,alpha*.5f);m.beam(hand,impact,width*.28,0xffffff,alpha);for(int i=0;i<4;i++)m.ring(hand.lerp(impact,i/4d),axis,width*2,.035,age*.1,c,alpha);}
            }
            case "ice","frost","icefall","shatter"->{
                for(int i=0;i<9;i++){double a=i*2.4;Vec3 p=end.add(Math.cos(a)*(i%3)*.65,-1,Math.sin(a)*(i%3)*.65);m.crystal(p,.18+(i%3)*.08,(.7+(i%4)*.4)*release,0x8edcff,alpha*.65f);}
                if(form.equals("frost"))m.ring(end,UP,1.5,.08,age*.04,0xe3ffff,alpha);
            }
            case "vortex","gravity","shadow","shadow_step","summon","swarm","poison","transfigure","resonance"->{
                int c=form.equals("poison")?0x98c456:form.equals("shadow")||form.equals("shadow_step")?0x353058:form.equals("resonance")?0xe95991:color;
                Vec3 at=Set.of("shadow","shadow_step","summon").contains(form)?start.add(0,-1.4,0):impact;
                for(int i=0;i<5;i++){double a=age*.13+i*Math.PI*2/5;double radius=(form.equals("vortex")?1.4*(1-i*.16):.8+i*.12);Vec3 p=at.add(Math.cos(a)*radius,form.equals("vortex")?i*.3:Math.sin(a*2)*.25,Math.sin(a)*radius);m.orb(p,.13+i*.025,c,alpha*.6f);m.arc(at.add(0,i*.13,0),form.equals("vortex")?axis:UP,radius,.035,a,c,alpha);}
                if(form.equals("shadow_step"))m.ring(end.add(0,-1.4,0),UP,1,.05,-age*.1,c,alpha);
                if(form.equals("resonance")){m.star(end,axis,1.1,.06,c,alpha);m.beam(hand,end,.025,c,alpha*.5f);}
            }
            case "roots","flowers","feather","insects"->{
                for(int i=0;i<8;i++){double a=i*Math.PI/4;Vec3 p=end.add(Math.cos(a)*1.5,-1,Math.sin(a)*1.5);if(form.equals("roots")){m.beam(p,p.add(-Math.cos(a)*.4,release*2,-Math.sin(a)*.4),.12,0x678b52,alpha);m.crystal(p.add(0,release,0),.16,.8,0x93bc65,alpha);}else {m.arc(p.add(0,.8+Math.sin(age*.1+i)*.4,0),UP,.25,.09,age*.1+i,form.equals("feather")?0x434761:0xe9b8dc,alpha);}}
            }
            case "projection","frame","doors","receipt","sky","metal","grave","gavel"->{
                int c=form.equals("sky")?0x8ed3ff:form.equals("metal")?0xd5e1ea:form.equals("grave")?0x817692:form.equals("gavel")?0xd0a56e:color;
                for(int i=0;i<5;i++){Vec3 p=form.equals("projection")?start.lerp(end,i/5d):end.add((i-2)*.6,Math.sin(i+age*.05)*.3,0);m.frame(p,axis,.35,.6,.035,c,alpha*(1-i*.12f));}
                if(form.equals("gavel"))m.box(impact,.6,.25,.25,c,alpha);
                if(form.equals("metal"))m.orb(impact,r,c,alpha*.5f);
            }
            case "star","constellation","heart","rope","chain"->{
                int c=form.equals("heart")?0xff70ad:form.equals("rope")?0x655479:form.equals("chain")?0xb7c8da:0xfab4ff;
                m.beam(hand,impact,.025,c,alpha*.5f);
                for(int i=0;i<5;i++){Vec3 p=hand.lerp(impact,i/4d);if(form.equals("chain")||form.equals("rope"))m.ring(p,axis,.15,.035,age*.08+i,c,alpha);else m.star(p,axis,.24,.035,c,alpha);}
                if(form.equals("heart")){m.orb(end.add(-.15,.15,0),.22,c,alpha);m.orb(end.add(.15,.15,0),.22,c,alpha);m.crystal(end.add(0,-.2,0),.26,.35,c,alpha);}
            }
            case "speech","sound","clap","shockwave","wind"->{
                int c=form.equals("speech")?0xdcb9ff:form.equals("wind")?0xb7eade:color;
                for(int i=0;i<5;i++){
                    Vec3 at=hand.lerp(end,Math.max(0,release-i*.08));
                    double radius=.3+i*.22+release*.5;
                    if(form.equals("wind"))m.arc(at,axis,radius,.045,age*.2+i,c,alpha);
                    else m.ring(at,axis,radius,.025,age*.05,c,alpha*(1-i*.12f));
                }
                if(form.equals("clap")){m.star(hand,axis,.6+release,.04,0xfff0bd,alpha);m.ring(start.add(0,-.5,0),UP,release*2,.05,0,color,alpha*.5f);}
                if(form.equals("speech")){m.frame(hand,axis,.16,.25,.025,c,alpha);m.beam(hand.add(0,-.3,0),hand.add(0,.3,0),.025,c,alpha);}
                if(form.equals("sound")){Vec3 last=hand;for(int i=1;i<=16;i++){Vec3 next=hand.lerp(impact,i/16d).add(0,Math.sin(i*.9-age*.4)*.2,0);m.beam(last,next,.025,c,alpha);last=next;}}
            }
            case "water"->{
                Vec3 across=axis.cross(UP).normalize();
                for(int i=0;i<12;i++){
                    double x=(i-5.5)*.28;Vec3 at=impact.add(across.scale(x)).add(0,Math.sin(x*2-age*.18)*.35,0);
                    m.arc(at,across,.5,.09,age*.08,0x39a9e8,alpha*.6f);
                    m.orb(at.add(0,.45,0),.06,0xd9fbff,alpha);
                }
                m.ring(end.add(0,-1,0),UP,release*2.5,.05,age*.03,0x8be7ff,alpha);
            }
            case "barrier","ritual"->{
                Vec3 at=start.add(0,-1.4,0);int c=form.equals("barrier")?0xf2d592:color;
                for(int i=0;i<3;i++)m.ring(at.add(0,i*.07,0),UP,.7+i*.3,.035,age*.04,c,alpha);
                for(int i=0;i<6;i++){
                    double a=i*Math.PI/3+age*.02;Vec3 point=at.add(Math.cos(a)*1.3,0,Math.sin(a)*1.3);
                    if(form.equals("barrier"))m.arc(at.add(0,.8,0),new Vec3(Math.cos(a),0,Math.sin(a)),1.2,.025,0,c,alpha*.5f);
                    else m.star(point,UP,.18,.025,c,alpha);
                }
            }
            case "heal"->{
                Vec3 at=start.add(0,-.4,0);for(int i=0;i<3;i++){
                    double a=i*Math.PI*2/3+age*.025;Vec3 p=at.add(Math.cos(a)*.7,Math.sin(age*.1+i)*.2,Math.sin(a)*.7);
                    m.beam(p.add(-.16,0,0),p.add(.16,0,0),.035,0x92ffce,alpha);m.beam(p.add(0,-.25,0),p.add(0,.25,0),.035,0x92ffce,alpha);
                }
                m.ring(at.add(0,-.8,0),UP,.7+release*.5,.035,age*.05,0x92ffce,alpha);
            }
            case "teleport","flight"->{
                if(form.equals("teleport"))for(var at:List.of(start,end)){m.ring(at,axis,.8,.04,age*.07,color,alpha);m.ring(at,axis,1,.025,-age*.07,color,alpha*.6f);}
                else for(int side:new int[]{-1,1})for(int i=0;i<5;i++){
                    Vec3 at=start.add(side*(.5+i*.2),Math.sin(age*.16)*.1-i*.06,.25);
                    m.beam(at,at.add(side*.45,-.3-i*.08,.2),.05,0xffe5b4,alpha);
                }
            }
            case "comedy","jackpot"->{
                Vec3 at=impact;for(int i=0;i<3;i++){
                    Vec3 p=at.add((i-1)*.6,Math.sin(age*.3+i)*.25,0);
                    if(form.equals("jackpot")){m.orb(p,.22,i==1?0xffd35a:0xff74b8,alpha*.7f);m.frame(p,axis,.28,.4,.03,0xf1d3ff,alpha);}
                    else {m.box(p,.14,.14,.03,i==1?0xffd35a:0x84e5c4,alpha);m.star(p.add(0,.5,0),axis,.22,.02,0xffe0a4,alpha);}
                }
            }
            default->m.ring(hand,axis,r,.04,age*.07,color,alpha);

        }
    }
    private static void domain(Mesh m,Net.Technique d,float age,float alpha){
        var p=VisualCatalog.get(d.path());Vec3 c=d.origin();double radius=Mth.clamp(d.radius(),6,20),grow=Math.min(1,age/12);int color=p.tint();
        // Open lattice and ground boundary preserve visibility of enemies and terrain.
        for(int i=0;i<3;i++)m.ring(c.add(0,.08+i*.12,0),UP,radius*grow,.035,age*.012+i,color,alpha*.6f);
        for(int i=0;i<8;i++){double a=i*Math.PI/4;Vec3 b=c.add(Math.cos(a)*radius,.1,Math.sin(a)*radius);m.beam(b,b.add(0,3*grow,0),.018,color,alpha*.25f);}
        switch(p.domain()){
            case "void"->{for(int i=0;i<24;i++){double a=i*2.4+age*.003;Vec3 b=c.add(Math.cos(a)*(radius-1),2+(i%5),Math.sin(a)*(radius-1));m.star(b,b.subtract(c).normalize(),.16,.018,0xc0eeff,alpha*.7f);}m.ring(c.add(0,5,0),UP,3,.08,age*.015,0x9890ff,alpha*.5f);}
            case "shrine"->{Vec3 b=c.add(0,0,4);for(int i:new int[]{-1,1})for(int j:new int[]{-1,1})m.box(b.add(i*1.7,1.5,j*.9),.15,1.5,.15,0xa63f4c,alpha*.6f);m.box(b.add(0,3,0),2.4,.15,1.5,0x563248,alpha*.8f);m.box(b.add(0,3.3,0),1.8,.15,1.1,0xc2a5a0,alpha*.6f);for(int i=0;i<8;i++)m.arc(c.add(Math.sin(i)*5,1,Math.cos(i)*5),FORWARD,1,.025,age*.02+i,0xffb0b0,alpha*.4f);}
            case "shadow"->{for(int i=0;i<8;i++)m.orb(c.add(Math.sin(i*2.4)*radius*.7,.12,Math.cos(i*2.4)*radius*.7),.6+Math.sin(age*.05+i)*.12,0x52416e,alpha*.4f);}
            case "blades"->{for(int i=0;i<16;i++){double a=i*Math.PI/8;Vec3 b=c.add(Math.cos(a)*(3+i%3),.1,Math.sin(a)*(3+i%3));m.beam(b,b.add(0,1.8,0),.035,0xc9e8ff,alpha);m.beam(b.add(-.2,1.3,0),b.add(.2,1.3,0),.04,0xe8c881,alpha);}}
            case "hands"->{for(int i=0;i<12;i++){Vec3 b=c.add(Math.sin(i*2.4)*(radius-1),1,Math.cos(i*2.4)*(radius-1));m.box(b,.2,.35,.1,0xa5a1bd,alpha*.5f);for(int j=0;j<4;j++)m.beam(b.add((j-1.5)*.12,.3,0),b.add((j-1.5)*.13,.75,0),.035,0xd5bed9,alpha*.6f);}}
            case "volcano"->{for(int i=0;i<12;i++)m.crystal(c.add(Math.sin(i)*radius*.85,.1,Math.cos(i)*radius*.85),.6,1.4+Math.sin(age*.05+i)*.4,0xff742e,alpha*.6f);}
            case "ocean"->{for(int i=0;i<5;i++)m.ring(c.add(0,.1+i*.1,0),UP,2+i*1.7+Math.sin(age*.06+i)*.2,.08,age*.01,0x5ad9f6,alpha*.45f);}
            case "womb","graves"->{for(int i=0;i<8;i++){Vec3 b=c.add(Math.sin(i)*5,.5,Math.cos(i)*5);if(p.domain().equals("graves"))m.box(b,.4,.7,.25,0x8d8aa1,alpha*.6f);else m.orb(b,.5,0xa46396,alpha*.5f);}}
            case "panels","court","jackpot"->{for(int i=0;i<8;i++){double a=i*Math.PI/4;Vec3 b=c.add(Math.cos(a)*5,1.5,Math.sin(a)*5);m.frame(b,b.subtract(c).normalize(),.75,1.1,.06,color,alpha*.55f);if(p.domain().equals("jackpot"))m.box(b,.25,.4,.1,(i%3==0)?0xffd35e:0xff72c7,alpha*.55f);}if(p.domain().equals("court")){m.beam(c.add(-2,3,3),c.add(2,3,3),.07,0xffdf9e,alpha);m.beam(c.add(0,1,3),c.add(0,4,3),.07,0xffdf9e,alpha);}}
            case "sphere"->{m.orb(c.add(0,3,4),1.5,0x605477,alpha*.5f);m.ring(c.add(0,3,4),UP,1.7,.06,age*.02,0xd9c2ff,alpha);}
            case "sumo"->m.ring(c.add(0,.1,0),UP,4,.12,0,0xe3c9a4,alpha);
            default->{}
        }
    }
    /** Position/color quad primitives. Fixed segment counts bound CPU and buffer cost. */
    private record Mesh(org.joml.Matrix4f matrix,VertexConsumer out){
        private void v(Vec3 p,int c,float a){out.vertex(matrix,(float)p.x,(float)p.y,(float)p.z).color((c>>16)&255,(c>>8)&255,c&255,(int)(Mth.clamp(a,0,1)*255)).endVertex();}
        void quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color,float alpha){v(a,color,alpha);v(b,color,alpha);v(c,color,alpha);v(d,color,alpha);}
        Vec3 tangent(Vec3 n){return n.cross(Math.abs(n.y)>.9?FORWARD:UP).normalize();}
        void beam(Vec3 a,Vec3 b,double width,int color,float alpha){Vec3 n=b.subtract(a).normalize();if(n.lengthSqr()<.1||width<=0)return;Vec3 u=tangent(n),v=n.cross(u);for(int i=0;i<6;i++){double x=i*Math.PI/3,y=(i+1)*Math.PI/3;Vec3 p=u.scale(Math.cos(x)*width).add(v.scale(Math.sin(x)*width)),q=u.scale(Math.cos(y)*width).add(v.scale(Math.sin(y)*width));quad(a.add(p),b.add(p),b.add(q),a.add(q),color,alpha);}}
        void ring(Vec3 c,Vec3 n,double r,double width,double angle,int color,float alpha){ribbon(c,n,r,width,angle,Math.PI*2,32,color,alpha);}
        void arc(Vec3 c,Vec3 n,double r,double width,double angle,int color,float alpha){ribbon(c,n,r,width,angle,Math.PI*1.25,18,color,alpha);}
        void ribbon(Vec3 c,Vec3 n,double r,double width,double angle,double length,int steps,int color,float alpha){Vec3 u=tangent(n),v=n.cross(u);for(int i=0;i<steps;i++){double a=angle+i*length/steps,b=angle+(i+1)*length/steps;Vec3 x=u.scale(Math.cos(a)).add(v.scale(Math.sin(a))),y=u.scale(Math.cos(b)).add(v.scale(Math.sin(b)));quad(c.add(x.scale(r-width)),c.add(y.scale(r-width)),c.add(y.scale(r+width)),c.add(x.scale(r+width)),color,alpha);}}
        void orb(Vec3 c,double r,int color,float alpha){if(r<=0)return;for(int i=0;i<6;i++){double a=-Math.PI/2+i*Math.PI/6,b=a+Math.PI/6;for(int j=0;j<12;j++){double x=j*Math.PI/6,y=x+Math.PI/6;quad(sphere(c,r,a,x),sphere(c,r,b,x),sphere(c,r,b,y),sphere(c,r,a,y),color,alpha);}}}
        Vec3 sphere(Vec3 c,double r,double a,double b){return c.add(Math.cos(a)*Math.cos(b)*r,Math.sin(a)*r,Math.cos(a)*Math.sin(b)*r);}
        void star(Vec3 c,Vec3 n,double r,double width,int color,float alpha){Vec3 u=tangent(n),v=n.cross(u);for(int i=0;i<5;i++){double a=i*Math.PI*2/5;Vec3 p=u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));beam(c.add(p.scale(.15)),c.add(p.scale(r)),width,color,alpha);}}
        void bolt(Vec3 a,Vec3 b,double phase,int color,float alpha){Vec3 n=b.subtract(a).normalize(),u=tangent(n),v=n.cross(u),last=a;for(int i=1;i<=8;i++){Vec3 p=a.lerp(b,i/8d).add(u.scale(i==8?0:Math.sin(i*8.7+phase)*.25)).add(v.scale(i==8?0:Math.cos(i*5.3+phase)*.2));beam(last,p,.025,color,alpha);last=p;}}
        void frame(Vec3 c,Vec3 n,double w,double h,double thick,int color,float alpha){Vec3 u=tangent(n).scale(w),v=n.cross(tangent(n)).scale(h);beam(c.add(u).add(v),c.subtract(u).add(v),thick,color,alpha);beam(c.subtract(u).add(v),c.subtract(u).subtract(v),thick,color,alpha);beam(c.subtract(u).subtract(v),c.add(u).subtract(v),thick,color,alpha);beam(c.add(u).subtract(v),c.add(u).add(v),thick,color,alpha);}
        void box(Vec3 c,double x,double y,double z,int color,float alpha){Vec3 a=c.add(-x,-y,-z),b=c.add(x,-y,-z),d=c.add(-x,y,-z),e=c.add(-x,-y,z),f=c.add(x,-y,z),g=c.add(x,y,z),h=c.add(-x,y,z),k=c.add(x,y,-z);quad(a,b,k,d,color,alpha);quad(e,h,g,f,color,alpha);quad(a,e,f,b,color,alpha);quad(d,k,g,h,color,alpha);quad(a,d,h,e,color,alpha);quad(b,f,g,k,color,alpha);}
        void crystal(Vec3 c,double radius,double height,int color,float alpha){if(height<=0)return;for(int i=0;i<4;i++){double a=i*Math.PI/2,b=a+Math.PI/2;Vec3 p=c.add(Math.cos(a)*radius,0,Math.sin(a)*radius),q=c.add(Math.cos(b)*radius,0,Math.sin(b)*radius),top=c.add(0,height,0);quad(p,q,top,top,color,alpha);}}
    }
}
