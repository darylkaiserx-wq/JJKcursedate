package dev.cursedfate;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;

/** Shared keyframed poses: all player paths and humanoid NPCs use the same synchronized timeline. */
public final class CombatVisuals {
    public record Clip(String name,long start,int length){}
    private static final Map<UUID,Clip> ACTIVE=new HashMap<>();
    public static void start(UUID actor,String name){var w=Minecraft.getInstance().level;if(w!=null)ACTIVE.put(actor,new Clip(name,w.getGameTime(),Combat.duration(name)));}
    public static void clear(){ACTIVE.clear();}
    public static void tick(){var w=Minecraft.getInstance().level;if(w==null){clear();return;}ACTIVE.values().removeIf(c->w.getGameTime()-c.start()>c.length());}
    public static Clip current(LivingEntity e){var c=ACTIVE.get(e.getUUID());if(c!=null&&e.level().getGameTime()-c.start()>c.length()){ACTIVE.remove(e.getUUID());return null;}return c;}
    public static float phase(LivingEntity e,float partial){var c=current(e);return c==null?0:Mth.clamp((e.level().getGameTime()-c.start()+partial)/c.length(),0,1);}
    // Piecewise keyframe interpolation gives a windup, a fast contact pose, then recovery.
    public static float envelope(float t){if(t<.3f)return t/.3f*.35f;if(t<.45f)return Mth.lerp((t-.3f)/.15f,.35f,1);return (1-t)/.55f;}
    /** Locomotion overlays vanilla stride for all roster models, with combat taking priority. */
    public static <T extends LivingEntity> void locomotion(PlayerModel<T> model,T actor,float walk,float amount){
        // setupAnim does not reset these pivots/rotations on every frame.
        model.body.xRot=actor.isCrouching()?.5f:0;
        if(!actor.onGround()||actor.isCrouching()||actor.isPassenger()||actor.isSwimming()||actor.isFallFlying()||amount<.01f)return;
        boolean running=actor.isSprinting()||actor.getDeltaMovement().horizontalDistanceSqr()>.065;
        float weight=Mth.clamp(amount,0,1),stride=(float)Math.cos(walk*(running?.9f:.6662f));
        model.body.xRot=(running?.19f:.035f)*weight;
        model.rightLeg.xRot=stride*(running?1.15f:.75f)*weight;model.leftLeg.xRot=-model.rightLeg.xRot;
        if(model.attackTime<=0&&!actor.isUsingItem()){
            model.rightArm.xRot=-stride*(running?.95f:.65f)*weight-(running?.3f:0);
            model.leftArm.xRot=stride*(running?.95f:.65f)*weight-(running?.3f:0);
        }
        model.jacket.copyFrom(model.body);model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);
    }
    public static <T extends LivingEntity> void pose(PlayerModel<T> model,T actor,float partial){
        var c=current(actor);if(c==null)return;float t=phase(actor,partial),f=envelope(t);
        switch(c.name()){
            case "quick","weapon"->{model.rightArm.xRot=-1.8f*f;model.rightArm.yRot=-.3f*f;model.body.yRot=-.18f*f;}
            case "heavy","black_flash"->{model.rightArm.xRot=-2.4f*f;model.rightArm.zRot=.25f*f;model.body.yRot=-.45f*f;model.body.xRot=.12f*f;model.leftArm.xRot=-.6f*f;}
            case "barrage"->{float wave=(float)Math.sin(t*Math.PI*12);model.rightArm.xRot=-1.3f-.7f*wave;model.leftArm.xRot=-1.3f+.7f*wave;model.body.yRot=wave*.12f;}
            default->{String path=actor instanceof Sorcerer s?s.character().id():Client.styles.getOrDefault(actor.getUUID(),Client.state.getString("path"));model.rightArm.xRot=-1.7f*f;model.leftArm.xRot=path.equals("gojo")?-.35f*f:-1.7f*f;model.rightArm.yRot=-.35f*f;model.leftArm.yRot=.35f*f;model.head.xRot+=.12f*f;}
        }
        model.jacket.copyFrom(model.body);model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);
    }
}
