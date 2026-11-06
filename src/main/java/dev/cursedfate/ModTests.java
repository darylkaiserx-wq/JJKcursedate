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
            h.assertBlockPresent(net.minecraft.world.level.block.Blocks.WHITE_CONCRETE,2,3,3);
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
}
