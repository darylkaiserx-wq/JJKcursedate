package dev.cursedfate;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class CityFeature extends Feature<NoneFeatureConfiguration> {
    public CityFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
        var origin=c.origin();var world=c.level();if(origin.getY()<world.getSeaLevel()||!world.getBlockState(origin.below()).isSolid())return false;
        build(world,origin,c.random().nextInt(3));return true;
    }
    public static void build(net.minecraft.world.level.LevelAccessor w,BlockPos o,int type){
        // All writes fit in the originating chunk; no terrain destruction by combat.
        for(int x=0;x<15;x++)for(int z=0;z<15;z++){
            set(w,o.offset(x,-1,z),Blocks.STONE_BRICKS.defaultBlockState());
            for(int y=0;y<17;y++)set(w,o.offset(x,y,z),Blocks.AIR.defaultBlockState());
            set(w,o.offset(x,0,z),(type==2?Blocks.GRAY_CONCRETE:Blocks.POLISHED_ANDESITE).defaultBlockState());
        }
        if(type==2){for(int bx:new int[]{1,9})for(int bz:new int[]{1,9})house(w,o.offset(bx,1,bz),5,12,true);}
        else if(type==0)house(w,o.offset(1,1,1),13,6,false);
        else {for(int x:new int[]{2,12})for(int z:new int[]{2,12})for(int y=1;y<6;y++)set(w,o.offset(x,y,z),Blocks.MOSSY_STONE_BRICKS.defaultBlockState());for(int x=5;x<10;x++)for(int z=5;z<10;z++)set(w,o.offset(x,1,z),Blocks.CRYING_OBSIDIAN.defaultBlockState());}
        for(int x:new int[]{0,14})for(int z:new int[]{0,14}){for(int y=1;y<4;y++)set(w,o.offset(x,y,z),Blocks.DARK_OAK_FENCE.defaultBlockState());set(w,o.offset(x,4,z),Blocks.SEA_LANTERN.defaultBlockState());}
    }
    private static void house(net.minecraft.world.level.LevelAccessor w,BlockPos o,int size,int height,boolean city){
        for(int x=0;x<size;x++)for(int z=0;z<size;z++)for(int y=0;y<=height;y++){
            boolean edge=x==0||z==0||x==size-1||z==size-1;
            if(y==height||y%4==0)set(w,o.offset(x,y,z),(city?Blocks.SMOOTH_STONE:Blocks.DARK_OAK_PLANKS).defaultBlockState());
            else if(edge)set(w,o.offset(x,y,z),(y%4==2?Blocks.GLASS_PANE:city?Blocks.WHITE_CONCRETE:Blocks.STRIPPED_DARK_OAK_LOG).defaultBlockState());
        }
        for(int y=1;y<=2;y++)set(w,o.offset(size/2,y,0),Blocks.AIR.defaultBlockState());
        // Accessible floors via a central ladder shaft.
        for(int y=1;y<height;y++){set(w,o.offset(0,y,1),(city?Blocks.WHITE_CONCRETE:Blocks.STRIPPED_DARK_OAK_LOG).defaultBlockState());set(w,o.offset(1,y,1),Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING,net.minecraft.core.Direction.EAST));}
        set(w,o.offset(size/2,1,size/2),Blocks.LANTERN.defaultBlockState());
        set(w,o.offset(size/2+1,1,size/2),Blocks.CRAFTING_TABLE.defaultBlockState());
    }
    private static void set(net.minecraft.world.level.LevelAccessor w,BlockPos p,BlockState s){w.setBlock(p,s,2);}
}
