package dev.cursedfate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.*;
import net.minecraft.sounds.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import java.util.*;

public final class Actions {
    public static void message(Player p,String s){p.displayClientMessage(Component.literal(s),true);}
    public static void request(ServerPlayer p,String action,int slot){
        if(!p.isAlive()||p.isSpectator())return;
        var d=Progress.data(p);long now=p.level().getGameTime();
        // Rate limit even failed requests. No client-supplied costs, damage, rank or target.
        if(now<d.getLong("requestAfter"))return;d.putLong("requestAfter",now+3);
        if(action.startsWith("choose:")){
            String id=action.substring(7);var c=Catalog.get(id);
            if(c==null||!d.getString("path").isEmpty()||!p.getInventory().contains(new ItemStack(CursedFate.BOOK.get())))return;
            d.putString("path",id);d.putFloat("energy",Progress.maxEnergy(p));
            for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(CursedFate.BOOK.get())){p.getInventory().getItem(i).shrink(1);break;}
            message(p,"Your fate: "+c.name()+" • Press K for your journal");Net.sync(p,false);Net.appearance(p);return;
        }
        if(action.equals("journal")){Net.sync(p,true);return;}
        if(Progress.character(p)==null){message(p,"Choose a character with your Fate Book first.");return;}
        if(Progress.heavenly(p)&&java.util.Set.of("rct","guard","reinforce","amplify","vow").contains(action)){message(p,"Heavenly Restriction: physical power replaces cursed energy.");return;}
        switch(action){
            case "vow" -> {if(now>=d.getLong("vowAfter")){d.putBoolean("vow",!d.getBoolean("vow"));d.putLong("vowAfter",now+200);message(p,"Binding Vow "+(d.getBoolean("vow")?"active: +30% technique damage, +20% damage taken":"released"));}}
            case "amplify" -> {boolean on=!d.getBoolean("amplify");d.putBoolean("amplify",on);message(p,"Domain Amplification "+(on?"enabled: disables innate techniques":"disabled"));}
            case "cast" -> cast(p,slot);
            case "summon" -> {
                if(!d.getString("path").equals("megumi")||Progress.level(p)<25||now<d.getLong("summonAfter"))break;
                if(d.getFloat("energy")>=70&&Summons.spawn(p,"mahoraga")){Progress.pay(p,70);d.putLong("summonAfter",now+600);}
            }
            case "domain" -> Domains.open(p);
            case "rct" -> {
                if(Progress.level(p)<24){message(p,"Reverse Cursed Technique unlocks at level 24.");break;}
                if(now<d.getLong("rctAfter")||p.getHealth()>=p.getMaxHealth())break;
                if(Progress.pay(p,40)){p.heal(8);d.putLong("rctAfter",now+160);pulse(p,"heal",8);}
            }
            case "guard" -> {if(now>=d.getLong("guardAfter")&&Progress.pay(p,25)){d.putLong("simpleUntil",now+100);d.putLong("guardAfter",now+240);p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,1));message(p,"Simple Domain • 5 seconds");}}
            case "reinforce" -> {boolean on=!d.getBoolean("reinforce");d.putBoolean("reinforce",on);message(p,"Reinforcement "+(on?"enabled":"disabled"));}
            case "mission" -> {if(d.getInt("missionKills")>=5){Progress.award(p,250);d.putInt("missionKills",0);p.getInventory().add(new ItemStack(CursedFate.FRAGMENT.get(),3));message(p,"Mission complete! +250 XP • +3 curse fragments");}else message(p,"Exorcism mission: "+d.getInt("missionKills")+" / 5 hostile kills");}
            case "promote" -> {
                int r=d.getInt("rank");
                if(r>=4){message(p,"You are already Special Grade.");break;}
                if(Progress.level(p)<Progress.requiredLevel(r+1)){message(p,"Required level: "+Progress.requiredLevel(r+1));break;}
                if(d.getInt("trialKills")<(r+1)*5){message(p,"Promotion trial: "+d.getInt("trialKills")+" / "+((r+1)*5)+" exorcisms");break;}
                d.putInt("rank",r+1);d.putInt("trialKills",0);message(p,"Promoted to "+Progress.rank(p)+"!");pulse(p,"heal",12);
            }
        }
        Net.sync(p,false);
    }
    public static void cast(ServerPlayer p,int slot){
        var c=Progress.character(p);if(c==null||slot<0||slot>=3)return;
        var m=c.moves().get(slot);var d=Progress.data(p);long now=p.level().getGameTime();
        if(d.getBoolean("amplify")||now<d.getLong("confiscatedUntil")){message(p,"Innate technique suppressed");return;}
        if(Progress.level(p)<m.level()){message(p,m.name()+" unlocks at level "+m.level());return;}
        if(now<d.getLong("cooldown"+slot)){message(p,"Technique cooling down");return;}
        int mastery=Progress.mastery(d.getInt("mastery"+slot));
        int cost=Math.max(0,Math.round(m.cost()*(1-(mastery-1)*.02f)));
        if(!Progress.pay(p,cost)){message(p,"Not enough cursed energy");return;}
        d.putLong("cooldown"+slot,now+m.cooldown());d.putLong("lastCast",now);
        if(aimed(p,22)!=null)d.putInt("mastery"+slot,Math.min(10000,d.getInt("mastery"+slot)+1));
        perform(p,m,(1+Progress.level(p)*.012f+(mastery-1)*.04f)*(d.getBoolean("vow")?1.3f:1));p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        message(p,m.name());
    }
    public static boolean enemy(LivingEntity caster,LivingEntity e){
        if(e==caster||!e.isAlive()||e.isAlliedTo(caster))return false;
        if(e instanceof Player pl&&(pl.isCreative()||pl.isSpectator()||!Settings.PVP.get()))return false;
        if(e instanceof Shikigami sh&&sh.isAlliedTo(caster))return false;
        if(e instanceof Wolf w&&w.isTame())return false;
        if(caster instanceof Sorcerer s){if(e instanceof Sorcerer other)return s.character().hostile()!=other.character().hostile();return s.character().hostile()?e instanceof Player:e instanceof net.minecraft.world.entity.monster.Monster;}
        return !(e instanceof Sorcerer s)||s.character().hostile();
    }
    public static List<LivingEntity> nearby(LivingEntity caster,double radius){
        return caster.level().getEntitiesOfClass(LivingEntity.class,caster.getBoundingBox().inflate(radius),e->enemy(caster,e));
    }
    public static LivingEntity aimed(LivingEntity caster,double range){
        Vec3 eye=caster.getEyePosition(),look=caster.getLookAngle();
        return nearby(caster,range).stream().filter(e->{Vec3 delta=e.getEyePosition().subtract(eye);return delta.normalize().dot(look)>.88&&caster.hasLineOfSight(e);}).min(Comparator.comparingDouble(caster::distanceToSqr)).orElse(null);
    }
    public static void perform(LivingEntity caster,Catalog.Move m,float scale){
        if(!(caster.level() instanceof ServerLevel world))return;
        var target=aimed(caster,22);float damage=m.power()*scale;
        pulse(caster,m.kind(),8);
        if(m.name().equals("Nue"))Summons.spawn(caster,"nue");
        switch(m.kind()){
            case "shield" -> {caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,160,1));caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,160,0));}
            case "heal" -> caster.heal(m.power());
            case "flight" -> {caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,200,0));caster.addEffect(new MobEffectInstance(MobEffects.LEVITATION,40,0));}
            case "teleport","rush" -> {
                Vec3 look=caster.getLookAngle().multiply(1,0,1).normalize();
                Vec3 end=caster.position().add(look.scale(m.kind().equals("rush")?5:8));
                var hit=world.clip(new ClipContext(caster.getEyePosition(),end.add(0,1,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster));
                end=hit.getLocation().subtract(look.scale(.8)).add(0,-1,0);
                AABB box=caster.getBoundingBox().move(end.subtract(caster.position()));
                if(world.getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(end))&&world.hasChunkAt(net.minecraft.core.BlockPos.containing(end))&&world.noCollision(caster,box)){
                    caster.teleportTo(end.x,end.y,end.z);caster.fallDistance=0;
                }
                if(m.kind().equals("rush"))for(var e:nearby(caster,3))hit(caster,e,damage);
            }
            case "summon" -> {
                Summons.spawn(caster,Summons.form(caster));
            }
            case "swap" -> {if(target!=null&&world.noCollision(caster,caster.getBoundingBox().move(target.position().subtract(caster.position())))&&world.noCollision(target,target.getBoundingBox().move(caster.position().subtract(target.position())))){Vec3 pos=caster.position();caster.teleportTo(target.getX(),target.getY(),target.getZ());target.teleportTo(pos.x,pos.y,pos.z);}}
            case "push","pull","gravity","water" -> {for(var e:nearby(caster,10)){Vec3 v=e.position().subtract(caster.position()).normalize().scale(m.kind().equals("pull")?-.9:1.1);e.push(v.x,.25,v.z);e.hurtMarked=true;hit(caster,e,damage*.5f);if(m.kind().equals("gravity"))e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,3));}}
            case "stun","ice","plant" -> {if(target!=null){target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,3));target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,1));hit(caster,target,damage*.6f);}}
            case "strike" -> {var e=aimed(caster,4);if(e!=null)hit(caster,e,damage*1.5f);}
            default -> {if(target!=null){beam(caster,target,m.kind());hit(caster,target,damage);switch(m.kind()){
                case "fire" -> target.setSecondsOnFire(4);
                case "poison" -> target.addEffect(new MobEffectInstance(MobEffects.POISON,100,0));
                case "curse" -> target.addEffect(new MobEffectInstance(MobEffects.WITHER,60,0));
                case "lightning" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2));
            }}}
        }
        world.playSound(null,caster.blockPosition(),m.kind().equals("heal")?SoundEvents.AMETHYST_BLOCK_CHIME:CursedFate.IMPACT.get(),SoundSource.PLAYERS,.7f,1.1f);
    }
    public static void hit(LivingEntity caster,LivingEntity target,float amount){
        if(!enemy(caster,target))return;
        target.hurt(caster.damageSources().indirectMagic(caster,caster),amount);
    }
    public static void pulse(LivingEntity e,String kind,int radius){
        if(!(e.level() instanceof ServerLevel w))return;
        ParticleOptions particle=switch(kind){case "fire"->ParticleTypes.FLAME;case "heal","plant"->ParticleTypes.HAPPY_VILLAGER;case "ice","water"->ParticleTypes.SNOWFLAKE;case "blood"->ParticleTypes.CRIMSON_SPORE;default->ParticleTypes.SOUL_FIRE_FLAME;};
        w.sendParticles(particle,e.getX(),e.getY()+1,e.getZ(),Settings.PARTICLES.get(),radius*.15,.7,radius*.15,.02);
    }
    public static void beam(LivingEntity from,LivingEntity target,String kind){
        if(!(from.level() instanceof ServerLevel w))return;
        int n=Math.min(Settings.PARTICLES.get(),24);Vec3 start=from.getEyePosition(),delta=target.getEyePosition().subtract(start);
        for(int i=0;i<n;i++){Vec3 p=start.add(delta.scale((double)i/Math.max(1,n)));w.sendParticles(kind.equals("fire")?ParticleTypes.FLAME:ParticleTypes.END_ROD,p.x,p.y,p.z,1,0,0,0,0);}
    }
}
