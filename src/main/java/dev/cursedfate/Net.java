package dev.cursedfate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Optional;
import java.util.function.Supplier;

public final class Net {
    private static final String VERSION="3";
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(CursedFate.id("main"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Request(String action,int slot){
        public void encode(FriendlyByteBuf b){b.writeUtf(action,64);b.writeInt(slot);}
        public static Request decode(FriendlyByteBuf b){return new Request(b.readUtf(64),b.readInt());}
        public void handle(Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->{var p=c.getSender();if(p!=null)Actions.request(p,action,slot);});c.setPacketHandled(true);}
    }
    public record State(CompoundTag data,boolean open){
        public void encode(FriendlyByteBuf b){b.writeNbt(data);b.writeBoolean(open);}
        public static State decode(FriendlyByteBuf b){return new State(b.readNbt(),b.readBoolean());}
        public void handle(Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->Client.receive(data,open));c.setPacketHandled(true);}
    }
    public record Appearance(java.util.UUID player,String path){
        public void encode(FriendlyByteBuf b){b.writeUUID(player);b.writeUtf(path,32);}
        public static Appearance decode(FriendlyByteBuf b){return new Appearance(b.readUUID(),b.readUtf(32));}
        public void handle(Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->Client.styles.put(player,path));c.setPacketHandled(true);}
    }
    public static void appearance(ServerPlayer p){CHANNEL.send(PacketDistributor.ALL.noArg(),new Appearance(p.getUUID(),Progress.data(p).getString("path")));}
    public record Animation(java.util.UUID actor,String clip){
        public void encode(FriendlyByteBuf b){b.writeUUID(actor);b.writeUtf(clip,32);}
        public static Animation decode(FriendlyByteBuf b){return new Animation(b.readUUID(),b.readUtf(32));}
        public void handle(Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->CombatVisuals.start(actor,clip));c.setPacketHandled(true);}
    }
    public static void animate(net.minecraft.world.entity.LivingEntity actor,String clip){if(!actor.level().isClientSide){CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->actor),new Animation(actor.getUUID(),clip));}}
    public static void init(){
        CHANNEL.registerMessage(3,Animation.class,Animation::encode,Animation::decode,Animation::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(2,Appearance.class,Appearance::encode,Appearance::decode,Appearance::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1,State.class,State::encode,State::decode,State::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void send(String action,int slot){CHANNEL.sendToServer(new Request(action,slot));}
    public static void sync(ServerPlayer p,boolean open){
        var d=Progress.data(p).copy();d.putInt("level",Progress.level(p));d.putInt("maxEnergy",Progress.maxEnergy(p));
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new State(d,open));
    }
}
