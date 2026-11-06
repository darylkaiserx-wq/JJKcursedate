package dev.cursedfate;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.*;

public final class Sorcerer extends PathfinderMob {
    private final String path;
    private static final EntityDataAccessor<Integer> CAST=SynchedEntityData.defineId(Sorcerer.class,EntityDataSerializers.INT);
    public Sorcerer(EntityType<? extends PathfinderMob> type,Level level,String path){super(type,level);this.path=path;
        setCustomNameVisible(true);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(character().hostile()?100:40);setHealth(getMaxHealth());
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(character().hostile()?7:4);
        if(!character().hostile())setPersistenceRequired();
    }
    @Override public boolean removeWhenFarAway(double distance){return character().hostile()&&!getPersistentData().hasUUID("companion");}
    public Catalog.Character character(){return Catalog.get(path);}
    public boolean casting(){return entityData.get(CAST)>0;}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(CAST,0);}
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.29).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,24).add(Attributes.ARMOR,4);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.1,true));
        goalSelector.addGoal(3,new Goal(){
            {setFlags(java.util.EnumSet.of(Flag.MOVE));}
            private Player owner;
            public boolean canUse(){if(!getPersistentData().hasUUID("companion"))return false;owner=level().getPlayerByUUID(getPersistentData().getUUID("companion"));return owner!=null&&distanceToSqr(owner)>36;}
            public void tick(){if(owner!=null)getNavigation().moveTo(owner,1.1);}
        });
        goalSelector.addGoal(5,new RandomStrollGoal(this,.7));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,LivingEntity.class,20,true,false,e->character()!=null&&!character().civilian()&&Actions.enemy(this,e)));
    }
    @Override public void aiStep(){super.aiStep();if(level().isClientSide)return;
        if(entityData.get(CAST)>0)entityData.set(CAST,entityData.get(CAST)-1);
        var t=getTarget();if(t!=null&&t.isAlive()&&tickCount%100==0&&distanceToSqr(t)<400&&hasLineOfSight(t)){
            if(character().hostile()&&getHealth()<getMaxHealth()*.5f&&tickCount%400==0)Domains.openNpc(this);
            lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,t.getEyePosition());
            var moves=character().moves();Actions.perform(this,moves.get(random.nextInt(3)),character().hostile()?1:.7f);entityData.set(CAST,20);
        }
    }
    @Override public boolean doHurtTarget(Entity target){swing(InteractionHand.MAIN_HAND);return super.doHurtTarget(target);}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){
        if(!level().isClientSide&&!character().hostile()){
            if(character().civilian())Actions.message(p,character().name()+": The city needs your help. Defeat curses and claim missions in your journal.");
            else if(character().role().equals("trainer")){Actions.message(p,character().name()+": Train your techniques. Promotion requires both experience and exorcisms.");if(p instanceof net.minecraft.server.level.ServerPlayer sp)Net.sync(sp,true);}
            else {var tag=getPersistentData();if(tag.hasUUID("companion")){if(tag.getUUID("companion").equals(p.getUUID())){tag.remove("companion");Actions.message(p,"Companion dismissed");}}else {
                long companions=level().getEntitiesOfClass(Sorcerer.class,p.getBoundingBox().inflate(64),s->s.getPersistentData().hasUUID("companion")&&s.getPersistentData().getUUID("companion").equals(p.getUUID())).size();
                if(companions>=3){Actions.message(p,"Dismiss a companion first (maximum three nearby).");return InteractionResult.SUCCESS;}
                tag.putUUID("companion",p.getUUID());Actions.message(p,character().name()+" will follow you.");}}
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source,int looting,boolean hit){super.dropCustomDeathLoot(source,looting,hit);if(character().hostile())spawnAtLocation(new ItemStack(CursedFate.FRAGMENT.get(),character().role().equals("boss")?4:1));}
}
