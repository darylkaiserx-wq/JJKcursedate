package dev.cursedfate;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.*;

public final class Domains {
    public record Domain(UUID owner,Vec3 center,long until,int strength,String path){}
    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>,List<Domain>> ACTIVE=new HashMap<>();
    public static void clear(){ACTIVE.clear();}
    public static void remove(UUID owner){ACTIVE.values().forEach(list->list.removeIf(d->d.owner().equals(owner)));}
    public static int count(ServerLevel world){return ACTIVE.getOrDefault(world.dimension(),List.of()).size();}
    public static void openNpc(Sorcerer npc){
        if(npc.character().domain().isEmpty())return;
        var list=ACTIVE.computeIfAbsent(npc.level().dimension(),k->new ArrayList<>());
        if(list.size()<8&&list.stream().noneMatch(d->d.center().distanceTo(npc.position())<Settings.DOMAIN_RADIUS.get()*2))list.add(new Domain(npc.getUUID(),npc.position(),npc.level().getGameTime()+160,25,npc.character().id()));
    }
    public static void open(ServerPlayer p){
        var c=Progress.character(p);var d=Progress.data(p);long now=p.level().getGameTime();
        if(d.getBoolean("amplify")||now<d.getLong("confiscatedUntil")){Actions.message(p,"Innate technique suppressed");return;}
        if(c==null||c.domain().isEmpty()){Actions.message(p,"This path has no innate domain. Use Simple Domain [G].");return;}
        if(Progress.level(p)<30){Actions.message(p,"Domain Expansion unlocks at level 30.");return;}
        var list=ACTIVE.computeIfAbsent(p.level().dimension(),k->new ArrayList<>());
        if(now<d.getLong("domainAfter")||list.size()>=8||!Progress.pay(p,100))return;
        d.putLong("domainAfter",now+1200);int strength=Progress.level(p)+d.getInt("rank")*5;
        boolean lose=false;for(var other:new ArrayList<>(list))if(other.center().distanceTo(p.position())<Settings.DOMAIN_RADIUS.get()*2){
            if(other.strength()>=strength)lose=true;
            if(other.strength()<=strength)list.remove(other);
        }
        if(lose){Actions.message(p,"Domain clash! Your barrier collapsed.");return;}
        list.add(new Domain(p.getUUID(),p.position(),now+240,strength,c.id()));
        Net.animate(p,"cast");Effects.at(p,"barrier");
        p.serverLevel().playSound(null,p.blockPosition(),CursedFate.DOMAIN_SOUND.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);
        p.serverLevel().players().stream().filter(x->x.distanceToSqr(p)<1600).forEach(x->x.displayClientMessage(Component.literal("Domain Expansion: "+c.domain()),false));
    }
    public static void tick(ServerLevel world){
        var list=ACTIVE.get(world.dimension());if(list==null)return;
        long now=world.getGameTime();
        list.removeIf(d->now>=d.until()||!(world.getEntity(d.owner()) instanceof LivingEntity e)||!e.isAlive()||e.position().distanceTo(d.center())>Settings.DOMAIN_RADIUS.get()*2);
        if(now%10!=0)return;
        for(var d:list){
            var owner=(LivingEntity)world.getEntity(d.owner());if(owner==null)continue;
            var c=Catalog.get(d.path());int radius=Settings.DOMAIN_RADIUS.get();
            if(Settings.PARTICLES.get()>0){
                var dust=new DustParticleOptions(new Vector3f(((c.tint()>>16)&255)/255f,((c.tint()>>8)&255)/255f,(c.tint()&255)/255f),1.8f);
                for(int i=0;i<Settings.PARTICLES.get();i++){double a=i*Math.PI*2/Settings.PARTICLES.get();world.sendParticles(dust,d.center().x+Math.cos(a)*radius,d.center().y+1+(i%3),d.center().z+Math.sin(a)*radius,1,0,.2,0,0);}
            }
            if(d.path().equals("hakari")){owner.heal(2);owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,20,1));}
            var bounds=new net.minecraft.world.phys.AABB(d.center().add(-radius,-radius,-radius),d.center().add(radius,radius,radius));
            for(var e:world.getEntitiesOfClass(LivingEntity.class,bounds,x->Actions.enemy(owner,x))){
                if(e.position().distanceTo(d.center())>radius||e instanceof Player p&&Progress.data(p).getLong("simpleUntil")>now)continue;
                switch(d.path()){
                    case "gojo","naoya"->e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,5));
                    case "jogo"->e.setSecondsOnFire(2);
                    case "higuruma"->{e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,30,2));if(e instanceof Player p)Progress.data(p).putLong("confiscatedUntil",now+20);}
                    case "miyo"->e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,1));
                    case "smallpox","mahito"->e.addEffect(new MobEffectInstance(MobEffects.WITHER,40,1));
                    default->{}
                }
                if(!java.util.Set.of("hakari","higuruma","miyo").contains(d.path()))Actions.hit(owner,e,2+d.strength()*.04f);
            }
        }
    }
}
