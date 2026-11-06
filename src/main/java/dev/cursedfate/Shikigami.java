package dev.cursedfate;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class Shikigami extends PathfinderMob {
    public final String form;
    public Shikigami(EntityType<? extends PathfinderMob> type,Level level,String form){super(type,level);this.form=form;setPersistenceRequired();}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.36).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.FOLLOW_RANGE,24);}
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData group,CompoundTag tag){
        if(reason==MobSpawnType.SPAWN_EGG)getPersistentData().putBoolean("creativeExhibit",true);
        return super.finalizeSpawn(world,difficulty,reason,group,tag);
    }
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(5,new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this,.7));goalSelector.addGoal(6,new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this,net.minecraft.world.entity.player.Player.class,8));}

    @Override public void aiStep(){
        super.aiStep();if(level().isClientSide)return;var d=getPersistentData();
        if(!d.hasUUID("owner")){if(d.getBoolean("creativeExhibit"))return;discard();return;}
        if(level().getGameTime()>=d.getLong("expires")){discard();return;}
        var owner=((net.minecraft.server.level.ServerLevel)level()).getEntity(d.getUUID("owner"));
        if(!(owner instanceof LivingEntity living)||!owner.isAlive()){discard();return;}
        if(distanceToSqr(owner)>4096){discard();return;}
        if(tickCount%10==0){
            var enemy=Actions.nearby(living,20).stream().filter(e->!(e instanceof Shikigami)).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            setTarget(enemy);Entity follow=enemy!=null?enemy:owner;
            boolean flying=form.equals("nue")||form.equals("moon_dregs")||form.equals("garuda");
            setNoGravity(flying);
            if(flying){var delta=follow.position().add(0,enemy!=null?1:2,0).subtract(position());if(delta.length()>2)setDeltaMovement(delta.normalize().scale(.35));else setDeltaMovement(getDeltaMovement().scale(.5));}
            else if(distanceToSqr(follow)>4)getNavigation().moveTo(follow,1.2);
            if(enemy!=null&&distanceToSqr(enemy)<(flying?9:6)&&tickCount%20==0&&hasLineOfSight(enemy)){
                swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                Actions.hit(living,enemy,form.equals("mahoraga")?12:6);
                if(form.equals("moon_dregs"))enemy.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,60,0));
                if(form.equals("nue"))Actions.beam(this,enemy,"lightning");
            }
        }
    }
    @Override public boolean isAlliedTo(Entity e){if(getPersistentData().hasUUID("owner")&&e.getUUID().equals(getPersistentData().getUUID("owner")))return true;return super.isAlliedTo(e);}
}
