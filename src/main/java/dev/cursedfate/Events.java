package dev.cursedfate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=CursedFate.ID)
public final class Events {
    private static final UUID SPEED=UUID.fromString("315857d2-0593-40cd-969f-68c4af45973c");
    private static final UUID HEALTH=UUID.fromString("6bac7c17-637a-4c90-9d97-bbf885f6f256");
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;var d=Progress.data(p);
        d.remove("requestAfter");
        if(!d.getBoolean("bookGiven")){if(!p.getInventory().add(new ItemStack(CursedFate.BOOK.get())))p.drop(new ItemStack(CursedFate.BOOK.get()),false);d.putBoolean("bookGiven",true);d.putFloat("energy",Progress.maxEnergy(p));}
        Net.sync(p,false);Net.appearance(p);
        for(var other:p.serverLevel().getServer().getPlayerList().getPlayers())Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->p),new Net.Appearance(other.getUUID(),Progress.data(other).getString("path")));
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event){
        var old=event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy();
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG,old);
        Progress.data(event.getEntity()).putFloat("energy",Progress.maxEnergy(event.getEntity()));
        Domains.remove(event.getOriginal().getUUID());
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){Domains.remove(event.getEntity().getUUID());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){Domains.remove(event.getEntity().getUUID());}
    @SubscribeEvent public static void stop(ServerStoppedEvent event){Domains.clear();}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer p)||p.tickCount%10!=0)return;
        var d=Progress.data(p);if(Progress.character(p)==null){p.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH);p.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED);d.putBoolean("reinforce",false);d.putBoolean("amplify",false);return;}
        float energy=d.getFloat("energy");boolean amplifying=d.getBoolean("amplify")&&energy>=5;
        d.putBoolean("amplify",amplifying);
        boolean reinforcing=d.getBoolean("reinforce")&&energy>=3;
        d.putFloat("energy",Math.min(Progress.maxEnergy(p),Math.max(0,energy+(amplifying?-5:reinforcing?-3:d.getBoolean("vow")?1:2))));
        if(reinforcing){p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,15,0,false,false));p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,15,0,false,false));}else d.putBoolean("reinforce",false);
        var health=p.getAttribute(Attributes.MAX_HEALTH);int rank=d.getInt("rank");double bonus=Math.min(20,rank*4)+(Progress.heavenly(p)?8:0);
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
        if(Progress.heavenly(p)){if(speed.getModifier(SPEED)==null)speed.addTransientModifier(new AttributeModifier(SPEED,"Heavenly speed",.25,AttributeModifier.Operation.MULTIPLY_TOTAL));}else speed.removeModifier(SPEED);
        var existing=health.getModifier(HEALTH);
        if(existing==null||existing.getAmount()!=bonus){health.removeModifier(HEALTH);health.addTransientModifier(new AttributeModifier(HEALTH,"Sorcerer vitality",bonus,AttributeModifier.Operation.ADDITION));}
        Net.sync(p,false);
    }
    @SubscribeEvent public static void levelTick(TickEvent.LevelTickEvent event){if(event.phase==TickEvent.Phase.END&&event.level instanceof net.minecraft.server.level.ServerLevel w)Domains.tick(w);}
    @SubscribeEvent public static void summonTick(LivingEvent.LivingTickEvent event){
        var e=event.getEntity();if(e instanceof Wolf&&!e.level().isClientSide&&e.getPersistentData().contains("expires")&&e.level().getGameTime()>=e.getPersistentData().getLong("expires"))e.discard();
    }
    @SubscribeEvent public static void kill(LivingDeathEvent event){
        var killed=event.getEntity();if(killed instanceof Player)Domains.remove(killed.getUUID());
        if(!(event.getSource().getEntity() instanceof ServerPlayer p))return;
        if(!(killed instanceof Monster)&&!(killed instanceof Sorcerer s&&s.character().hostile()))return;
        if(Progress.character(p)==null)return;
        var d=Progress.data(p);Progress.award(p,killed instanceof Sorcerer?100:30);d.putInt("missionKills",Math.min(5,d.getInt("missionKills")+1));d.putInt("trialKills",Math.min(100,d.getInt("trialKills")+1));Net.sync(p,false);
    }
    @SubscribeEvent public static void hurt(LivingHurtEvent event){
        if(event.getEntity() instanceof ServerPlayer p){
            var d=Progress.data(p);if(d.getBoolean("vow"))event.setAmount(event.getAmount()*1.2f);
            if(d.getString("path").equals("gojo")&&d.getBoolean("reinforce")&&!(event.getSource().getEntity() instanceof Player a&&Progress.data(a).getBoolean("amplify"))&&Progress.pay(p,8))event.setAmount(event.getAmount()*.4f);
            event.setAmount(event.getAmount()*(1-Math.min(4,d.getInt("rank"))*.04f));
        }
        if(event.getSource().getEntity() instanceof ServerPlayer p&&event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)&&Progress.character(p)!=null){
            var d=Progress.data(p);long now=p.level().getGameTime();
            // A melee hit within 10 ticks of a technique is a timed Black Flash, with a cooldown.
            if(now-d.getLong("lastCast")<=10&&d.contains("lastCast")&&now>=d.getLong("flashAfter")&&Progress.pay(p,12)){
                event.setAmount(event.getAmount()*1.8f);d.putLong("flashAfter",now+100);Progress.award(p,12);Actions.pulse(event.getEntity(),"blood",3);Actions.message(p,"BLACK FLASH!");
            }
        }
    }
}
