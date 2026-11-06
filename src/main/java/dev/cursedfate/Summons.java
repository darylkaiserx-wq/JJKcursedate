package dev.cursedfate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
public final class Summons {
    public static String form(LivingEntity caster){
        String path=caster instanceof net.minecraft.world.entity.player.Player p?Progress.data(p).getString("path"):caster instanceof Sorcerer s?s.character().id():"";
        return switch(path){case "megumi"->"divine_dog";case "yuta","rika"->"rika_manifestation";case "yuki"->"garuda";case "junpei"->"moon_dregs";case "dagon"->"death_swarm";default->"cursed_spirit";};
    }
    public static boolean spawn(LivingEntity owner,String form){
        if(!(owner.level() instanceof ServerLevel w)||!CursedFate.SUMMONS.containsKey(form))return false;
        var existing=w.getEntitiesOfClass(Shikigami.class,owner.getBoundingBox().inflate(64),e->e.getPersistentData().hasUUID("owner")&&e.getPersistentData().getUUID("owner").equals(owner.getUUID()));
        if(existing.size()>=2)return false;
        var summon=CursedFate.SUMMONS.get(form).get().create(w);if(summon==null)return false;
        var look=owner.getLookAngle().multiply(1,0,1).normalize();
        var pos=owner.position().add(look.scale(2)).add(-look.z*existing.size()*1.5,0,look.x*existing.size()*1.5);
        summon.moveTo(pos.x,pos.y,pos.z,owner.getYRot(),0);
        if(!w.noCollision(summon))return false;
        summon.getPersistentData().putUUID("owner",owner.getUUID());summon.getPersistentData().putLong("expires",w.getGameTime()+600);
        w.addFreshEntity(summon);return true;
    }
}
