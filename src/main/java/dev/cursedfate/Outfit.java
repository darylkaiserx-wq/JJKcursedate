package dev.cursedfate;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
public final class Outfit extends ArmorItem {
    private final String outfit;
    public Outfit(String outfit,Type type){super(ArmorMaterials.IRON,type,new Item.Properties());this.outfit=outfit;}
    @Override public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){return CursedFate.ID+":textures/models/armor/"+outfit+"_layer_"+(slot==EquipmentSlot.LEGS?2:1)+".png";}
}
