package dev.cursedfate;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber(modid=CursedFate.ID)
public final class WorldContent {
    @Mod.EventBusSubscriber(modid=CursedFate.ID,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent public static void placements(net.minecraftforge.event.entity.SpawnPlacementRegisterEvent e){
            for(var entry:CursedFate.NPCS.entrySet())if(Catalog.get(entry.getKey()).hostile())e.register(entry.getValue().get(),net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL&&world.getMaxLocalRawBrightness(pos)<=7&&world.getBlockState(pos.below()).isSolid(),net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
        @SubscribeEvent public static void register(RegisterEvent event){event.register(ForgeRegistries.Keys.FEATURES,h->h.register(CursedFate.id("city_site"),new CityFeature()));}
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){
        var root=Commands.literal("jjk");
        root.then(Commands.literal("help").executes(c->{c.getSource().sendSuccess(()->Component.literal("JJK: /jjk status | book | cast <1-3> | domain | rct | guard | mission | promote. Admin: spawn <id>, structure <city|residence|ruins>, xp <amount>, reset."),false);return 1;}));
        root.then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();c.getSource().sendSuccess(()->Component.literal(Progress.rank(p)+" • Level "+Progress.level(p)+" • CE "+(int)Progress.data(p).getFloat("energy")),false);return 1;}));
        root.then(Commands.literal("book").executes(c->{var p=c.getSource().getPlayerOrException();if(Progress.character(p)==null&&!p.getInventory().contains(new net.minecraft.world.item.ItemStack(CursedFate.BOOK.get())))p.getInventory().add(new net.minecraft.world.item.ItemStack(CursedFate.BOOK.get()));Net.sync(p,true);return 1;}));
        root.then(Commands.literal("cast").then(Commands.argument("slot",IntegerArgumentType.integer(1,3)).executes(c->{Actions.request(c.getSource().getPlayerOrException(),"cast",IntegerArgumentType.getInteger(c,"slot")-1);return 1;})));
        for(String action:java.util.List.of("domain","rct","guard","mission","promote","reinforce","summon","vow","amplify"))root.then(Commands.literal(action).executes(c->{Actions.request(c.getSource().getPlayerOrException(),action,0);return 1;}));
        root.then(Commands.literal("spawn").requires(s->s.hasPermission(2)).then(Commands.argument("character",StringArgumentType.word()).suggests((c,b)->{CursedFate.NPCS.keySet().forEach(b::suggest);return b.buildFuture();}).executes(c->{var type=CursedFate.NPCS.get(StringArgumentType.getString(c,"character"));if(type==null)return 0;var npc=type.get().create(c.getSource().getLevel());if(npc==null)return 0;var pos=c.getSource().getPosition();npc.moveTo(pos.x+2,pos.y,pos.z,0,0);c.getSource().getLevel().addFreshEntity(npc);return 1;})));
        root.then(Commands.literal("xp").requires(s->s.hasPermission(2)).then(Commands.argument("amount",IntegerArgumentType.integer(0,1000000)).executes(c->{var p=c.getSource().getPlayerOrException();Progress.award(p,IntegerArgumentType.getInteger(c,"amount"));Net.sync(p,false);return 1;})));
        root.then(Commands.literal("reset").requires(s->s.hasPermission(2)).executes(c->{var p=c.getSource().getPlayerOrException();Progress.data(p).putString("path","");Domains.remove(p.getUUID());if(!p.getInventory().contains(new net.minecraft.world.item.ItemStack(CursedFate.BOOK.get())))p.getInventory().add(new net.minecraft.world.item.ItemStack(CursedFate.BOOK.get()));Net.sync(p,true);Net.appearance(p);return 1;}));
        root.then(Commands.literal("structure").requires(s->s.hasPermission(2)).then(Commands.argument("type",StringArgumentType.word()).suggests((c,b)->{for(String s:java.util.List.of("city","residence","ruins"))b.suggest(s);return b.buildFuture();}).executes(c->{String kind=StringArgumentType.getString(c,"type");if(!java.util.Set.of("city","residence","ruins").contains(kind))return 0;var pos=net.minecraft.core.BlockPos.containing(c.getSource().getPosition()).offset(4,0,4);CityFeature.build(c.getSource().getLevel(),pos,kind.equals("city")?2:kind.equals("residence")?0:1);return 1;})));
        e.getDispatcher().register(root);
    }
}
