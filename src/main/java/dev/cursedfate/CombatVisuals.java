package dev.cursedfate;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;

/** Character-directed windup, contact and recovery poses, shared by players and NPCs. */
public final class CombatVisuals {
    public record Clip(String name,long start,int length){
        public boolean casting(){return name.startsWith("cast")||name.equals("domain");}
        public int slot(){return name.startsWith("cast:")?Mth.clamp(Integer.parseInt(name.substring(5)),0,2):0;}
    }
    private static final Map<UUID,Clip> ACTIVE=new HashMap<>();
    public static void start(UUID actor,String name){var w=Minecraft.getInstance().level;if(w!=null&&Set.of("quick","heavy","barrage","black_flash","weapon","cast","cast:0","cast:1","cast:2","domain").contains(name))ACTIVE.put(actor,new Clip(name,w.getGameTime(),Combat.duration(name)));}
    public static void clear(){ACTIVE.clear();}
    public static void tick(){var w=Minecraft.getInstance().level;if(w==null){clear();return;}ACTIVE.values().removeIf(c->w.getGameTime()-c.start()>c.length());}
    public static Clip current(LivingEntity e){var c=ACTIVE.get(e.getUUID());if(c!=null&&e.level().getGameTime()-c.start()>c.length()){ACTIVE.remove(e.getUUID());return null;}return c;}
    public static float phase(LivingEntity e,float partial){var c=current(e);return c==null?0:Mth.clamp((e.level().getGameTime()-c.start()+partial)/c.length(),0,1);}
    public static String path(LivingEntity actor){return actor instanceof Sorcerer s?s.character().id():actor==Minecraft.getInstance().player?Client.state.getString("path"):Client.styles.getOrDefault(actor.getUUID(),"");}
    public static VisualCatalog.Profile profile(LivingEntity actor){return VisualCatalog.get(path(actor));}
    public static float envelope(float t){if(t<.3f)return t/.3f*.35f;if(t<.45f)return Mth.lerp((t-.3f)/.15f,.35f,1);return (1-t)/.55f;}
    private static <T extends LivingEntity> void sleeves(PlayerModel<T> m){m.jacket.copyFrom(m.body);m.rightSleeve.copyFrom(m.rightArm);m.leftSleeve.copyFrom(m.leftArm);m.rightPants.copyFrom(m.rightLeg);m.leftPants.copyFrom(m.leftLeg);}
    /** Gait respects swimming, flight, mounts, crouching and item use. */
    public static <T extends LivingEntity> void locomotion(PlayerModel<T> m,T actor,float walk,float amount){
        m.body.xRot=actor.isCrouching()?.5f:0;
        if(!actor.onGround()||actor.isCrouching()||actor.isPassenger()||actor.isSwimming()||actor.isFallFlying()||amount<.01f)return;
        var p=profile(actor);boolean running=actor.isSprinting()||actor.getDeltaMovement().horizontalDistanceSqr()>.065;
        float weight=Mth.clamp(amount,0,1),stride=Mth.cos(walk*(running?.9f:.6662f)*p.tempo());
        m.body.xRot=(running?.12f+p.lean():p.lean()*.25f)*weight;
        m.body.yRot=Mth.sin(walk*.5f)*p.spread()*.15f*weight;
        m.rightLeg.xRot=stride*(running?1.15f:.75f)*weight;m.leftLeg.xRot=-m.rightLeg.xRot;
        if(m.attackTime<=0&&!actor.isUsingItem()){
            float swing=running?.7f+p.guard():.65f;
            m.rightArm.xRot=-stride*swing*weight-(running?p.guard():0);
            m.leftArm.xRot=stride*swing*weight-(running?p.guard():0);
            // Sword users keep their weapon hand steady; sprinters carry it behind the hip.
            if(Set.of("blade","draw","ratio","staff","hammer").contains(p.gesture())&&running){
                if(p.lead()>0)m.rightArm.xRot=.45f+stride*.2f;else m.leftArm.xRot=.45f-stride*.2f;
            }
            if(p.gesture().equals("shadow")&&running){m.rightArm.xRot=.65f;m.leftArm.xRot=.65f;}
        }
        sleeves(m);
    }
    public static <T extends LivingEntity> void pose(PlayerModel<T> m,T actor,float partial){
        var clip=current(actor);if(clip==null)return;var p=profile(actor);
        float t=phase(actor,partial),f=envelope(t),lead=p.lead();
        m.rightArm.yRot=0;m.leftArm.yRot=0;m.rightArm.zRot=.05f;m.leftArm.zRot=-.05f;
        switch(clip.name()){
            case "quick","weapon"->{m.rightArm.xRot=(-1.6f-p.guard())*f;m.rightArm.yRot=-p.spread()*f;m.leftArm.xRot=-p.guard()*2*f;m.body.yRot=-lead*(.14f+p.lean())*f;}
            case "heavy","black_flash"->{m.rightArm.xRot=(-2.1f-p.guard())*f;m.rightArm.zRot=p.spread()*f;m.body.yRot=-lead*(.3f+p.lean())*f;m.body.xRot=p.lean()*f;m.leftArm.xRot=-.8f*f;m.rightLeg.xRot=-.25f*f;}
            case "barrage"->{float wave=Mth.sin(t*Mth.PI*12*p.tempo());m.rightArm.xRot=-1.3f-.7f*wave;m.leftArm.xRot=-1.3f+.7f*wave;m.body.yRot=lead*wave*(.1f+p.lean());m.leftLeg.xRot=-.25f;m.rightLeg.xRot=.25f;}
            default->{
                m.rightArm.xRot=-1.45f*f;m.leftArm.xRot=-1.45f*f;m.rightArm.yRot=-p.spread()*f;m.leftArm.yRot=p.spread()*f;m.body.yRot=lead*p.lean()*f;
                if(clip.name().equals("domain")){m.rightArm.xRot=-1.8f*f;m.leftArm.xRot=-1.8f*f;m.rightArm.yRot=-.65f*f;m.leftArm.yRot=.65f*f;}
                else switch(p.gesture()){
                    case "finger"->{m.rightArm.xRot=(-1.5f-clip.slot()*.3f)*f;m.rightArm.yRot=-.15f*f;m.leftArm.xRot=(clip.slot()==2?-1.7f:-.3f)*f;m.leftArm.yRot=.6f*f;}
                    case "cleave"->{m.rightArm.xRot=-1.8f*f;m.rightArm.zRot=(clip.slot()==0?-1.1f:.6f)*f;m.leftArm.xRot=-.5f*f;m.body.yRot=-.4f*f;}
                    case "blood","prayer"->{m.rightArm.xRot=-1.55f*f;m.leftArm.xRot=-1.55f*f;m.rightArm.yRot=-.55f*f;m.leftArm.yRot=.55f*f;m.rightArm.zRot=-.12f*f;m.leftArm.zRot=.12f*f;}
                    case "shadow"->{m.rightArm.xRot=-1.3f*f;m.leftArm.xRot=-1.3f*f;m.rightArm.yRot=-.8f*f;m.leftArm.yRot=.8f*f;m.body.xRot=.1f*f;}
                    case "clap"->{float close=Mth.sin(t*Mth.PI);m.rightArm.xRot=-1.4f*f;m.leftArm.xRot=-1.4f*f;m.rightArm.yRot=-close*.9f;m.leftArm.yRot=close*.9f;}
                    case "blade","draw","ratio","staff","sweep"->{float sweep=t<.4f?-.6f:f;m.rightArm.xRot=-1.6f*f;m.rightArm.zRot=sweep*1.1f;m.leftArm.xRot=-.65f*f;m.body.yRot=-sweep*.45f;}
                    case "hammer","gavel"->{m.rightArm.xRot=-2.8f*f;m.rightArm.zRot=.25f*f;m.leftArm.xRot=-1.2f*f;m.body.xRot=.15f*f;}
                    case "speech"->{m.rightArm.xRot=-2.1f*f;m.rightArm.yRot=-.7f*f;m.leftArm.xRot=-.25f*f;m.head.xRot=-.12f*f;}
                    case "aim","cannon"->{m.rightArm.xRot=-Mth.HALF_PI*f;m.leftArm.xRot=-1.25f*f;m.leftArm.yRot=.8f*f;m.head.xRot=-.1f*f;}
                    case "orb","heart","star"->{m.rightArm.xRot=-1.2f*f;m.leftArm.xRot=-1.2f*f;m.rightArm.zRot=-.65f*f;m.leftArm.zRot=.65f*f;}
                    case "ritual","tide"->{m.rightArm.xRot=-1.1f*f;m.leftArm.xRot=-1.1f*f;m.rightArm.zRot=-1.2f*f;m.leftArm.zRot=1.2f*f;m.head.xRot=-.15f*f;}
                    case "frame","claw"->{m.rightArm.xRot=-1.8f*f;m.leftArm.xRot=-.8f*f;m.rightArm.zRot=-.65f*f;m.body.yRot=-.4f*f;}
                    case "sumo","brawler"->{m.rightArm.xRot=-1.9f*f;m.leftArm.xRot=-.9f*f;m.rightLeg.xRot=.35f*f;m.leftLeg.xRot=-.35f*f;m.body.xRot=.2f*f;}
                    default->{}
                }
                m.head.xRot+=p.lean()*.3f*f;
            }
        }
        // Mirrored lead is authored per character, and never swaps the held item itself.
        if(p.lead()<0){float x=m.rightArm.xRot,y=m.rightArm.yRot,z=m.rightArm.zRot;m.rightArm.xRot=m.leftArm.xRot;m.rightArm.yRot=-m.leftArm.yRot;m.rightArm.zRot=-m.leftArm.zRot;m.leftArm.xRot=x;m.leftArm.yRot=-y;m.leftArm.zRot=-z;}
        sleeves(m);
    }
    /** Small first-person choreography offsets, applied before vanilla arm-space transforms. */
    public static void hand(com.mojang.blaze3d.vertex.PoseStack pose,LivingEntity actor,Clip clip,float sign,float f){
        var p=profile(actor);float side=sign*p.lead();float slot=clip.slot();
        switch(p.gesture()){
            case "finger","speech"->pose.translate(side*.04,sign<0?-.12*f:.12*f,-f*(sign>0?.18:.02));
            case "blood","shadow","prayer","clap"->pose.translate(-sign*.18*f,.09*f,-.14*f);
            case "blade","draw","staff","sweep","cleave","ratio"->{pose.translate(sign*.16*f,.08*f,-.08*f);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(sign*35*f));}
            case "hammer","gavel"->{pose.translate(0,.22*f,.08*f);pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-35*f));}
            case "ritual","tide","orb","star","heart"->pose.translate(sign*.18*f,.02*f,-.07*f);
            default->pose.translate(sign*p.spread()*.1*f,p.lean()*f,-.04*f);
        }
        if(clip.casting())pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(sign*slot*6*f));
    }
}
