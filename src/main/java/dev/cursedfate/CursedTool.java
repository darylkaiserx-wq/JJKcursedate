package dev.cursedfate;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
public final class CursedTool extends SwordItem {
    private final String tool;
    public CursedTool(String tool){super(Tiers.DIAMOND,4,-2.4f,new Item.Properties().durability(1561));this.tool=tool;}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){
        if(!attacker.level().isClientSide){
            Net.animate(attacker,"weapon");Effects.at(target,"slash");
            switch(tool){
                case "inverted_spear","black_rope" -> {target.removeEffect(MobEffects.DAMAGE_RESISTANCE);target.removeEffect(MobEffects.DAMAGE_BOOST);}
                case "soul_split_katana","executioner_sword" -> target.hurt(attacker.damageSources().magic(),3);
                case "kamutoke" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2));
                case "festering_life_sword" -> target.addEffect(new MobEffectInstance(MobEffects.POISON,80,0));
                case "playful_cloud" -> target.push(-Math.sin(Math.toRadians(attacker.getYRot()))*.8,.3,Math.cos(Math.toRadians(attacker.getYRot()))*.8);
                case "dragon_bone" -> {if(attacker instanceof Player p&&Progress.pay(p,8))target.hurt(attacker.damageSources().playerAttack(p),4);}
                case "chain_of_a_thousand_miles" -> {var v=attacker.position().subtract(target.position()).normalize();target.push(v.x,.2,v.z);}
            }
        }
        return super.hurtEnemy(stack,target,attacker);
    }
}
