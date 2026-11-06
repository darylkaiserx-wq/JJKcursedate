package dev.cursedfate;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import java.util.*;

@Mod(CursedFate.ID)
public final class CursedFate {
    public static final String ID="jjkcursedate";
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,ID);
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> IMPACT=SOUNDS.register("impact",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(id("impact")));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> DOMAIN_SOUND=SOUNDS.register("domain",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(id("domain")));
    public static final Map<String,RegistryObject<net.minecraft.sounds.SoundEvent>> AUDIO=new LinkedHashMap<>();
    static {for(String name:List.of("quick","heavy","barrage","charge","black_flash","slash","fire","lightning","blood"))AUDIO.put(name,SOUNDS.register(name,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(id(name))));}
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,ID);
    public static final Map<String,RegistryObject<net.minecraft.world.level.block.Block>> MATERIALS=new LinkedHashMap<>();
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,ID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,ID);
    public static final Map<String,RegistryObject<EntityType<Sorcerer>>> NPCS=new LinkedHashMap<>();
    public static final Map<String,RegistryObject<EntityType<Shikigami>>> SUMMONS=new LinkedHashMap<>();
    public static final Map<String,RegistryObject<Item>> TOOLS=new LinkedHashMap<>();
    public static final RegistryObject<Item> BOOK=ITEMS.register("fate_book",()->new Item(new Item.Properties().stacksTo(1)){
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            if(player instanceof net.minecraft.server.level.ServerPlayer sp)Net.sync(sp,true);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
        }
    });
    public static final RegistryObject<Item> FRAGMENT=ITEMS.register("curse_fragment",()->new Item(new Item.Properties()));
    static {
        for(String name:List.of("plaster","roof_tiles","timber_panel","curse_stone","paving")){
            var block=BLOCKS.register(name,()->new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE_BRICKS)));
            MATERIALS.put(name,block);ITEMS.register(name,()->new BlockItem(block.get(),new Item.Properties()));
        }
        for(String form:List.of("divine_dog","nue","rika_manifestation","cursed_spirit","moon_dregs","garuda","death_swarm","mahoraga"))SUMMONS.put(form,ENTITIES.register(form,()->EntityType.Builder.<Shikigami>of((type,world)->new Shikigami(type,world,form),MobCategory.MISC).sized(form.equals("mahoraga")?1.3f:.8f,form.equals("mahoraga")?2.8f:1.2f).clientTrackingRange(8).build(ID+":"+form)));
        for(var entry:SUMMONS.entrySet())ITEMS.register(entry.getKey()+"_spawn_egg",()->new ForgeSpawnEggItem(entry.getValue(),0x404553,0xd3bb95,new Item.Properties()));
        for(var c:Catalog.ALL){
            var entity=ENTITIES.register(c.id(),()->EntityType.Builder.<Sorcerer>of((type,world)->new Sorcerer(type,world,c.id()),c.hostile()?MobCategory.MONSTER:MobCategory.CREATURE)
                .sized(.6f,1.9f).clientTrackingRange(8).build(ID+":"+c.id()));
            NPCS.put(c.id(),entity);
            ITEMS.register(c.id()+"_spawn_egg",()->new ForgeSpawnEggItem(entity,c.tint(),0x252333,new Item.Properties()));
        }
        for(String tool:List.of("slaughter_demon","playful_cloud","inverted_spear","soul_split_katana","dragon_bone","black_rope","chain_of_a_thousand_miles","nanami_blade","festering_life_sword","executioner_sword","hiten","kamutoke","nobara_hammer","mechamaru_blade"))
            TOOLS.put(tool,ITEMS.register(tool,()->new CursedTool(tool)));
        for(String outfit:List.of("tokyo_uniform","kyoto_uniform","gojo_outfit","sukuna_robes","toji_outfit","yuta_uniform","nanami_suit","geto_robes"))
            for(var type:ArmorItem.Type.values())ITEMS.register(outfit+"_"+type.getName(),()->new Outfit(outfit,type));
    }
    public static final RegistryObject<CreativeModeTab> TAB=TABS.register("cursed_fate",()->CreativeModeTab.builder()
        .title(Component.literal("JJK • Cursed Fate")).icon(()->new ItemStack(BOOK.get()))
        .displayItems((p,out)->ITEMS.getEntries().forEach(i->out.accept(i.get()))).build());
    public static final RegistryObject<CreativeModeTab> EGGS=TABS.register("spawn_eggs",()->CreativeModeTab.builder().title(Component.literal("JJK • Characters & Summons")).icon(()->new ItemStack(ITEMS.getEntries().stream().filter(i->i.getId().getPath().equals("gojo_spawn_egg")).findFirst().orElseThrow().get())).displayItems((p,out)->ITEMS.getEntries().stream().filter(i->i.getId().getPath().endsWith("_spawn_egg")).forEach(i->out.accept(i.get()))).build());
    public CursedFate(){
        var bus=FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);ENTITIES.register(bus);TABS.register(bus);SOUNDS.register(bus);BLOCKS.register(bus);Effects.TYPES.register(bus);
        bus.addListener(this::attributes);
        Net.init();
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,Settings.SPEC);
    }
    private void attributes(EntityAttributeCreationEvent event){NPCS.values().forEach(e->event.put(e.get(),Sorcerer.attributes().build()));SUMMONS.values().forEach(e->event.put(e.get(),Shikigami.attributes().build()));}
    public static ResourceLocation id(String path){return new ResourceLocation(ID,path);}
}
