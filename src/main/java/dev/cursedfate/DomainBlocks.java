package dev.cursedfate;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Air-only temporary hemispheres. Scheduled ticks persist in chunks and clean orphaned blocks. */
public final class DomainBlocks {
    public static final class Barrier extends Block {
        public Barrier(){super(Properties.of().strength(-1,3600000).noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).lightLevel(s->5));}
        @Override public void onPlace(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,BlockState previous,boolean moving){if(level instanceof ServerLevel world)world.scheduleTick(pos,this,260);}
        @Override public void tick(BlockState state,ServerLevel world,BlockPos pos,net.minecraft.util.RandomSource random){world.removeBlock(pos,false);}
    }
    private record Key(ServerLevel world,UUID owner){}
    private static final class Shell {
        final List<BlockPos> pending=new ArrayList<>();final Set<BlockPos> placed=new HashSet<>();int cursor;
    }
    private static final Map<Key,Shell> SHELLS=new HashMap<>();
    public static void grow(ServerLevel world,Domains.Domain domain){
        if(!Settings.DOMAIN_BLOCKS.get())return;
        var shell=SHELLS.computeIfAbsent(new Key(world,domain.owner()),k->{
            var s=new Shell();int r=Settings.DOMAIN_RADIUS.get();var center=BlockPos.containing(domain.center());
            for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
                int horizontal=x*x+z*z;if(horizontal>r*r)continue;
                s.pending.add(center.offset(x,-1,z));
                for(int y=0;y<=r;y++){double dist=horizontal+y*y;if(dist>=(r-.8)*(r-.8)&&dist<=(r+.2)*(r+.2))s.pending.add(center.offset(x,y,z));}
            }
            s.pending.sort(Comparator.comparingDouble(p->{double dx=p.getX()-center.getX(),dz=p.getZ()-center.getZ();return dx*dx+dz*dz;}));return s;
        });
        // Bounded growth spreads from the center outward, including the rounded roof and floor.
        for(int n=0;n<128&&shell.cursor<shell.pending.size();n++,shell.cursor++){
            var pos=shell.pending.get(shell.cursor);
            if(!world.hasChunkAt(pos)||world.isOutsideBuildHeight(pos)||!world.getWorldBorder().isWithinBounds(pos)||!world.getBlockState(pos).isAir())continue;
            if(!world.getEntities(null,new net.minecraft.world.phys.AABB(pos)).isEmpty())continue;
            if(world.setBlock(pos,CursedFate.DOMAIN_BARRIER.get().defaultBlockState(),3)){
                shell.placed.add(pos);
            }
        }
    }
    public static void remove(ServerLevel world,UUID owner){
        var shell=SHELLS.remove(new Key(world,owner));if(shell==null)return;
        for(var pos:shell.placed)if(world.hasChunkAt(pos)&&world.getBlockState(pos).is(CursedFate.DOMAIN_BARRIER.get()))world.removeBlock(pos,false);
        // Unloaded chunks retain their scheduled self-cleanup ticks.
    }
    public static int placed(ServerLevel world,UUID owner){var s=SHELLS.get(new Key(world,owner));return s==null?0:s.placed.size();}
}
