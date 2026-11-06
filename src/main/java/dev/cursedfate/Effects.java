package dev.cursedfate;

import java.util.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.*;

public final class Effects {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> TYPES=DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES,CursedFate.ID);
    public static final Map<String,RegistryObject<SimpleParticleType>> SPRITES=new LinkedHashMap<>();
    static {for(String name:List.of("black_flash","blood_orb","blue_orb","red_orb","purple_orb","slash","fire","ice","lightning","curse","heal","barrier"))SPRITES.put(name,TYPES.register(name,()->new SimpleParticleType(false)));}
    public static String sprite(String kind){return switch(kind){case "blood"->"blood_orb";case "pull"->"blue_orb";case "push"->"red_orb";case "blast"->"purple_orb";case "strike","rush"->"slash";case "water"->"ice";case "shield","stun"->"barrier";case "plant"->"heal";default->SPRITES.containsKey(kind)?kind:"curse";};}
    public static void at(LivingEntity e,String kind){
        if(e.level() instanceof ServerLevel w&&Settings.PARTICLES.get()>0)w.sendParticles(SPRITES.get(sprite(kind)).get(),e.getX(),e.getY()+1,e.getZ(),Math.min(6,Settings.PARTICLES.get()),.5,.4,.5,.03);
    }
}
