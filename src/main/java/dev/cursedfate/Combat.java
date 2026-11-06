package dev.cursedfate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class Combat {
    public static int duration(String clip){return switch(clip){case "quick"->8;case "heavy"->20;case "barrage"->28;case "black_flash"->18;case "weapon"->14;default->24;};}
    public static boolean punch(ServerPlayer p,String kind){
        if(!java.util.Set.of("quick","heavy","barrage").contains(kind))return false;
        var d=Progress.data(p);long now=p.level().getGameTime();
        if(now<d.getLong("punchAfter"))return false;
        int cost=kind.equals("barrage")?16:kind.equals("heavy")?8:0;
        if(!Progress.heavenly(p)&&!Progress.pay(p,cost))return false;
        d.putLong("punchAfter",now+duration(kind));d.putLong("combatStarted",now);d.putString("combatClip",kind);
        if(kind.equals("barrage")){d.putLong("barrageNext",now+3);d.putInt("barrageLeft",6);}
        else if(kind.equals("heavy"))d.putLong("heavyAt",now+8);
        Net.animate(p,kind);
        if(kind.equals("quick"))strike(p,4,false,false);return true;
    }
    public static void tick(ServerPlayer p){
        var d=Progress.data(p);long now=p.level().getGameTime();
        if(!p.isAlive()||p.isSpectator()){cancel(p);return;}
        if(d.getLong("heavyAt")>0&&now>=d.getLong("heavyAt")){d.remove("heavyAt");strike(p,11,true,false);}
        if(d.getInt("barrageLeft")>0&&now>=d.getLong("barrageNext")){d.putInt("barrageLeft",d.getInt("barrageLeft")-1);d.putLong("barrageNext",now+4);strike(p,2.5f,false,true);}
    }
    public static void cancel(net.minecraft.world.entity.player.Player p){var d=Progress.data(p);for(String key:java.util.List.of("heavyAt","barrageLeft","barrageNext","combatStarted","combatClip"))d.remove(key);}
    private static void strike(ServerPlayer p,float damage,boolean heavy,boolean barrage){
        var target=Actions.aimed(p,3.4);if(target==null)return;
        // Only this bounded six-hit combo bypasses hurt cooldowns; unrelated attacks retain vanilla immunity.
        var type=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,CursedFate.id("barrage"));
        var source=barrage?new net.minecraft.world.damagesource.DamageSource(p.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(type),p):p.damageSources().playerAttack(p);
        if(!target.hurt(source,damage+(Progress.heavenly(p)?2:0)))return;
        if(heavy){Vec3 push=target.position().subtract(p.position()).normalize().scale(.8);target.push(push.x,.2,push.z);target.hurtMarked=true;}
        Effects.at(target,"slash");p.serverLevel().playSound(null,target.blockPosition(),CursedFate.AUDIO.get(heavy?"heavy":barrage?"barrage":"quick").get(),net.minecraft.sounds.SoundSource.PLAYERS,.8f,1);
    }
}
