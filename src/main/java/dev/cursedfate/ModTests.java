package dev.cursedfate;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;

@GameTestHolder(CursedFate.ID)
@PrefixGameTestTemplate(false)
public final class ModTests {
    private static net.minecraft.server.level.ServerPlayer mockServerPlayer(GameTestHelper h){
        var p=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"fate-test"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p);
        return p;
    }

    @GameTest(template="empty") public static void heavenlyAndRequestValidation(GameTestHelper h){
        var p=mockServerPlayer(h);var d=Progress.data(p);
        Actions.request(p,"choose:gojo",0);h.assertTrue(d.getString("path").isEmpty(),"Selection requires a book");
        d.putString("path","toji");d.putFloat("energy",0);
        h.assertTrue(Progress.maxEnergy(p)==0,"Heavenly Restriction has no cursed energy");
        h.assertTrue(Catalog.get("toji").moves().stream().allMatch(m->m.cost()==0),"Physical techniques need no CE");
        h.assertTrue(Progress.pay(p,0),"Physical move remains usable");
        d.putString("path","yuji");d.putFloat("energy",50);d.remove("requestAfter");
        Actions.request(p,"cast",1);h.assertTrue(d.getFloat("energy")==50&&!d.contains("cooldown1"),"Locked move does not consume energy");
        d.remove("requestAfter");Actions.request(p,"cast",99);h.assertTrue(d.getFloat("energy")==50,"Invalid move rejected");h.succeed();
    }
    @GameTest(template="empty") public static void domainClash(GameTestHelper h){
        Domains.clear();var a=mockServerPlayer(h);var b=mockServerPlayer(h);
        a.moveTo(h.absolutePos(new BlockPos(1,2,1)),0,0);b.moveTo(h.absolutePos(new BlockPos(2,2,2)),0,0);
        for(var p:java.util.List.of(a,b)){h.getLevel().addNewPlayer(p);Progress.data(p).putString("path","gojo");Progress.data(p).putInt("xp",20000);Progress.data(p).putFloat("energy",300);}
        Domains.open(a);h.assertTrue(Domains.count(h.getLevel())==1,"First barrier created");
        Domains.open(b);h.assertTrue(Domains.count(h.getLevel())==0,"Equal strength domains cancel");a.discard();b.discard();h.succeed();
    }
    @GameTest(template="city_test",timeoutTicks=80) public static void accessibleCityFloors(GameTestHelper h){
        CityFeature.build(h.getLevel(),h.absolutePos(new BlockPos(1,1,1)),2);
        h.runAfterDelay(20,()->{
            h.assertBlockPresent(net.minecraft.world.level.block.Blocks.GRAY_CONCRETE,1,1,1);
            h.assertBlockPresent(net.minecraft.world.level.block.Blocks.LADDER,3,3,3);
            h.assertBlockPresent(CursedFate.MATERIALS.get("plaster").get(),2,3,3);
            h.assertBlockPresent(net.minecraft.world.level.block.Blocks.LADDER,3,11,3);
            h.succeed();
        });
    }
    @GameTest(template="empty") public static void progression(GameTestHelper h){
        h.assertTrue(Progress.mastery(0)==1&&Progress.mastery(648)==10,"Technique mastery progression and cap");
        h.assertTrue(Progress.level(-1)==1,"Negative XP must not underflow");
        h.assertTrue(Progress.level(980)==8,"Grade 3 level boundary");
        h.assertTrue(Progress.level(1000000)==60,"Level cap");
        var p=h.makeMockSurvivalPlayer();Progress.data(p).putFloat("energy",20);
        h.assertTrue(!Progress.pay(p,21),"Insufficient energy denied");h.assertTrue(Progress.pay(p,20),"Exact energy accepted");
        h.assertTrue(Progress.data(p).getFloat("energy")==0,"Energy cannot become negative");
        Progress.award(p,500);h.assertTrue(Progress.data(p).getInt("xp")==500,"XP persists in player NBT");h.succeed();
    }
    @GameTest(template="empty") public static void rosterAndEquipment(GameTestHelper h){
        h.assertTrue(Catalog.ALL.size()==68,"Complete build roster registered");
        for(var c:Catalog.ALL){var e=CursedFate.NPCS.get(c.id()).get().create(h.getLevel());h.assertTrue(e!=null&&e.getMaxHealth()>0,"NPC valid: "+c.id());h.assertTrue(e.character().id().equals(c.id()),"NPC identity");}
        h.assertTrue(CursedFate.TOOLS.size()==14,"Tools registered");h.succeed();
    }
    @GameTest(template="empty") public static void summonsBounded(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();p.moveTo(h.absolutePos(new BlockPos(2,2,2)),0,0);
        var regular=h.spawn(EntityType.WOLF,1,2,1); // An ordinary wolf must not crash summon ownership lookup.
        var move=Catalog.get("megumi").moves().get(0);
        for(int i=0;i<5;i++)Actions.perform(p,move,1);
        var summons=h.getLevel().getEntitiesOfClass(Shikigami.class,p.getBoundingBox().inflate(64),w->w.getPersistentData().hasUUID("owner")&&w.getPersistentData().getUUID("owner").equals(p.getUUID()));
        h.assertTrue(summons.size()==2,"Maximum two temporary summons per caster");summons.forEach(net.minecraft.world.entity.Entity::discard);regular.discard();h.succeed();
    }
    @GameTest(template="empty") public static void selectionConsumesBook(GameTestHelper h){
        var p=mockServerPlayer(h);p.getInventory().add(new ItemStack(CursedFate.BOOK.get()));
        Actions.request(p,"choose:gojo",0);
        h.assertTrue(Progress.data(p).getString("path").equals("gojo"),"Path selected");
        h.assertTrue(!p.getInventory().contains(new ItemStack(CursedFate.BOOK.get())),"Starter book consumed");
        Progress.data(p).remove("requestAfter");p.getInventory().add(new ItemStack(CursedFate.BOOK.get()));Actions.request(p,"choose:sukuna",0);
        h.assertTrue(Progress.data(p).getString("path").equals("gojo"),"Replacement book cannot switch chosen path");h.succeed();
    }
    @GameTest(template="empty") public static void domainLifecycle(GameTestHelper h){
        var p=mockServerPlayer(h);p.moveTo(h.absolutePos(new BlockPos(2,2,2)),0,0);
        var d=Progress.data(p);d.putString("path","gojo");d.putInt("xp",20000);d.putFloat("energy",300);
        h.getLevel().addNewPlayer(p);Domains.open(p);h.assertTrue(Domains.count(h.getLevel())>=1,"Domain opens");
        h.assertTrue(d.getFloat("energy")==200,"Domain energy deducted");Domains.remove(p.getUUID());
        h.assertTrue(Domains.count(h.getLevel())==0,"Domain cleaned up");p.discard();h.succeed();
    }
    @GameTest(template="empty") public static void friendlyFireAndProtection(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var ally=CursedFate.NPCS.get("nobara").get().create(h.getLevel());var boss=CursedFate.NPCS.get("mahito").get().create(h.getLevel());
        h.assertTrue(!Actions.enemy(p,ally),"Friendly NPCs protected");h.assertTrue(Actions.enemy(p,boss),"Antagonists targetable");
        var other=h.makeMockSurvivalPlayer();h.assertTrue(!Actions.enemy(p,other),"PVP disabled by default");h.succeed();
    }

    @GameTest(template="empty") public static void selectedTechniqueAndEggs(GameTestHelper h){
        var p=mockServerPlayer(h);var d=Progress.data(p);d.putString("path","gojo");d.putFloat("energy",100);
        for(int expected:new int[]{1,2,0}){d.remove("requestAfter");Actions.request(p,"cycle",0);h.assertTrue(d.getInt("selectedTechnique")==expected,"Z cycles all three slots and wraps");}
        d.putInt("selectedTechnique",2);d.remove("requestAfter");Actions.request(p,"castSelected",0);
        h.assertTrue(!d.contains("cooldown2")&&d.getFloat("energy")==100,"X cannot bypass selected move unlock");
        d.putInt("selectedTechnique",0);d.remove("requestAfter");Actions.request(p,"castSelected",99);
        h.assertTrue(d.contains("cooldown0")&&d.getFloat("energy")<100,"X casts server selection, ignores client slot");
        h.assertTrue(CursedFate.ITEMS.getEntries().stream().filter(e->e.get().asItem() instanceof net.minecraft.world.item.SpawnEggItem).count()==76,"68 character and 8 summon eggs");
        h.assertTrue(CursedFate.MATERIALS.size()==5,"Five architecture materials registered");
        var egg=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(CursedFate.id("divine_dog_spawn_egg"));
        var exhibit=((net.minecraft.world.item.SpawnEggItem)egg).getType(new ItemStack(egg).getTag()).spawn(h.getLevel(),new ItemStack(egg),null,h.absolutePos(new BlockPos(2,2,2)),net.minecraft.world.entity.MobSpawnType.SPAWN_EGG,false,false);
        h.assertTrue(exhibit instanceof Shikigami,"Creative egg creates summon model");((Shikigami)exhibit).aiStep();h.assertTrue(!exhibit.isRemoved(),"Egg spawn survives without temporary owner");exhibit.discard();h.succeed();
    }
    private static net.minecraft.world.entity.animal.Pig punchTarget(GameTestHelper h,net.minecraft.server.level.ServerPlayer p){
        p.moveTo(h.absolutePos(new BlockPos(2,2,1)),0,0);
        var target=h.spawn(EntityType.PIG,2,2,3);target.setNoAi(true);target.setNoGravity(true);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);target.setHealth(100);
        // Aim at the pig's lower eye position.
        p.setXRot(22);return target;
    }
    @GameTest(template="empty",timeoutTicks=60) public static void boundedBarrage(GameTestHelper h){
        var p=mockServerPlayer(h);var d=Progress.data(p);d.putString("path","yuji");d.putFloat("energy",100);var target=punchTarget(h,p);
        h.assertTrue(Combat.punch(p,"barrage"),"Barrage accepted");h.assertTrue(d.getFloat("energy")==84,"Pay once for combo");
        h.assertTrue(!Combat.punch(p,"quick"),"Cannot spam during combo");h.onEachTick(()->Combat.tick(p));
        h.runAfterDelay(27,()->{h.assertTrue(Math.abs(target.getHealth()-85)<.01,"All six 2.5 damage hits land");h.assertTrue(d.getInt("barrageLeft")==0,"Barrage ends after six hits");});
        h.runAfterDelay(35,()->{h.assertTrue(Math.abs(target.getHealth()-85)<.01,"No extra hits after combo");target.discard();h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=60) public static void heavyWindupAndCancellation(GameTestHelper h){
        var p=mockServerPlayer(h);var d=Progress.data(p);d.putString("path","yuji");d.putFloat("energy",7);
        h.assertTrue(!Combat.punch(p,"heavy"),"Heavy requires energy");d.putFloat("energy",100);var target=punchTarget(h,p);
        h.assertTrue(Combat.punch(p,"heavy"),"Heavy queued");h.assertTrue(target.getHealth()==100,"Windup deals no early damage");
        h.onEachTick(()->Combat.tick(p));h.runAfterDelay(9,()->{h.assertTrue(target.getHealth()==89,"Heavy lands after windup");});
        h.runAfterDelay(25,()->{Combat.punch(p,"barrage");Combat.cancel(p);});
        h.runAfterDelay(52,()->{h.assertTrue(d.getInt("barrageLeft")==0&&!d.contains("heavyAt"),"Cancellation clears delayed attacks");h.assertTrue(target.getHealth()==89,"Cancelled combo deals no damage");target.discard();h.succeed();});
    }
    @GameTest(template="empty") public static void creativeSelectionWithoutBook(GameTestHelper h){
        var p=mockServerPlayer(h);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);var d=Progress.data(p);
        Actions.request(p,"choose:gojo",0);h.assertTrue(d.getString("path").equals("gojo"),"Creative menu needs no book");
        d.putInt("xp",20000);d.putInt("mastery0",88);d.putInt("barrageLeft",6);d.remove("requestAfter");Actions.request(p,"choose:toji",0);
        h.assertTrue(d.getString("path").equals("toji")&&Progress.maxEnergy(p)==0,"Creative can switch path");
        h.assertTrue(d.getInt("mastery0")==0&&d.getInt("barrageLeft")==0,"Switch clears path mastery and queued punches");h.assertTrue(d.getInt("xp")==20000,"Overall experience preserved");
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);d.remove("requestAfter");Actions.request(p,"choose:gojo",0);
        h.assertTrue(d.getString("path").equals("toji"),"Survival cannot switch paths");h.succeed();
    }
    @GameTest(template="city_test",timeoutTicks=100) public static void generatedCursedLoot(GameTestHelper h){
        var level=h.getLevel();var origin=h.absolutePos(new BlockPos(1,1,1));
        var parameters=new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,net.minecraft.world.phys.Vec3.atCenterOf(origin))
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        for(int type=0;type<3;type++){
            CityFeature.build(level,origin,type);
            var pos=origin.offset(type==0?8:type==1?7:4,2,type==0?8:type==1?7:4);
            var entity=level.getBlockEntity(pos);
            h.assertTrue(entity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,"Loot chest present at site "+type);
            h.assertTrue(!level.getBlockState(pos.above()).isRedstoneConductor(level,pos.above()),"Chest lid is unobstructed at site "+type);
            var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)entity;
            String expected="jjkcursedate:chests/"+(type==0?"residence":type==1?"ruins":"city");
            h.assertTrue(chest.saveWithFullMetadata().getString("LootTable").equals(expected),"Loot table persisted: "+expected);
            chest.unpackLootTable(null);
            boolean fragment=false;for(int slot=0;slot<chest.getContainerSize();slot++)if(chest.getItem(slot).is(CursedFate.FRAGMENT.get()))fragment=true;
            h.assertTrue(fragment,"Opening the chest rolls fragments at each site");
        }
        var seen=new java.util.HashSet<net.minecraft.world.item.Item>();
        var table=level.getServer().getLootData().getLootTable(CursedFate.id("chests/ruins"));
        for(int seed=1;seed<=1500;seed++)for(var stack:table.getRandomItems(parameters,seed))seen.add(stack.getItem());
        for(var tool:CursedFate.TOOLS.values())h.assertTrue(seen.contains(tool.get()),"Loot can produce "+tool.getId());
        for(var relic:CursedFate.RELICS.values())h.assertTrue(seen.contains(relic.get()),"Loot can produce "+relic.getId());
        h.succeed();
    }
    @GameTest(template="empty") public static void visualDirectionsAndPackets(GameTestHelper h){
        var signatures=new java.util.HashSet<String>();
        for(var c:Catalog.ALL){
            var profile=VisualCatalog.get(c.id());
            h.assertTrue(profile.moves().size()==c.moves().size(),"Visual slot coverage: "+c.id());
            h.assertTrue(profile.domain().equals("none")==c.domain().isEmpty(),"Domain art coverage: "+c.id());
            h.assertTrue(signatures.add(profile.gesture()+":"+profile.tempo()+":"+profile.guard()+":"+profile.lean()+":"+profile.spread()),"Individual motion profile: "+c.id());
            for(int slot=-1;slot<3;slot++){
                var packet=new Net.Technique(java.util.UUID.randomUUID(),c.id(),slot,new net.minecraft.world.phys.Vec3(1.25,70,-2.5),new net.minecraft.world.phys.Vec3(5,71,10),slot<0?240:36,slot<0?12:0);
                var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                try{packet.encode(buffer);h.assertTrue(packet.equals(Net.Technique.decode(buffer)),"Lossless visual endpoint/slot packet: "+c.id());}finally{buffer.release();}
            }
        }
        h.assertTrue(VisualCatalog.get("gojo").moves().equals(java.util.List.of("blue","red","purple")),"Limitless has distinct directions");
        h.succeed();
    }
    @GameTest(template="empty") public static void domainOwnerCleanup(GameTestHelper h){
        Domains.clear();var npc=CursedFate.NPCS.get("sukuna").get().create(h.getLevel());
        npc.moveTo(h.absolutePos(new BlockPos(1,2,1)),0,0);h.getLevel().addFreshEntity(npc);
        Domains.openNpc(npc);h.assertTrue(Domains.count(h.getLevel())==1,"NPC domain opens");
        Domains.remove(npc.getUUID());h.assertTrue(Domains.count(h.getLevel())==0,"Owner removal clears the domain and broadcasts its visual stop");
        Domains.openNpc(npc);npc.discard();Domains.tick(h.getLevel());
        h.assertTrue(Domains.count(h.getLevel())==0,"Missing owner expires the domain");h.succeed();
    }

    @GameTest(template="city_test") public static void movementVisualEndpoints(GameTestHelper h){
        var p=mockServerPlayer(h);Progress.data(p).putString("path","megumi");
        p.moveTo(h.absolutePos(new BlockPos(4,3,4)),0,0);var origin=p.getEyePosition();
        p.setPos(p.getX(),p.getY(),p.getZ()+4.2);
        var packet=Net.techniquePacket(p,Catalog.get("megumi").moves().get(2),null,origin);
        h.assertTrue(packet.origin().equals(origin)&&packet.end().equals(p.getEyePosition()),"Shadow Step visuals use the actual completed movement, including collision rejection");
        Progress.data(p).putString("path","dagon");
        var tide=Net.techniquePacket(p,Catalog.get("dagon").moves().get(0),null,p.getEyePosition());
        h.assertTrue(tide.end().equals(tide.origin()),"Radial water effect stays at the caster");
        Progress.data(p).putString("path","yuji");
        var strike=Net.techniquePacket(p,Catalog.get("yuji").moves().get(0),null,p.getEyePosition());
        h.assertTrue(strike.origin().distanceTo(strike.end())<=4.01,"Melee visual cannot imply a22-block hit");h.succeed();
    }

}
