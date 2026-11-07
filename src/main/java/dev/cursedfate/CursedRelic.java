package dev.cursedfate;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Collectible lore objects; no sealing, reincarnation or possession mechanics are implied. */
public final class CursedRelic extends Item {
    public CursedRelic(){super(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE));}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable("item.jjkcursedate.relic_description").withStyle(ChatFormatting.GRAY));
    }
}
