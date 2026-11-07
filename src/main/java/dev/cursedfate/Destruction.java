package dev.cursedfate;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/** Explicit per-move terrain capability; support moves never inherit it from a shared effect kind. */
public final class Destruction {
    private static final Map<String,int[]> RADII=new HashMap<>();
    static {
        String rows="gojo:2,3,4;yuji:1,2,0;sukuna:2,2,4;megumi:0,2,0;nobara:1,2,0;maki:2,2,2;toge:0,2,0;panda:1,2,2;nanami:1,3,0;yuta:0,0,4;geto:0,0,4;mahito:0,2,0;jogo:1,3,4;hanami:2,2,0;dagon:2,0,2;toji:1,0,2;choso:1,2,0;todo:0,1,1;kamo:1,0,0;momo:1,0,1;mai:1,0,1;miwa:1,1,0;mechamaru:3,1,0;gakuganji:1,2,0;kusakabe:1,0,1;mei_mei:2,0,2;naobito:1,0,0;naoya:1,0,3;yuki:3,0,4;kenjaku:0,2,4;uraume:2,0,3;haruta:1,0,1;eso:0,0,1;kechizu:0,1,0;finger_bearer:2,1,3;grasshopper:1,1,1;rika:2,4,0;higuruma:1,0,0;hakari:1,0,2;kashimo:2,1,3;hara:1,0,2;hanezoki:2,2,0;ryu:3,3,2;uro:0,3,0;kurourushi:1,0,0;dhruv:0,2,0;yorozu:2,0,4;daido:2,0,3;miyo:1,1,0;miguel:1,0,0";
        for(String row:rows.split(";")){var pair=row.split(":");RADII.put(pair[0],Arrays.stream(pair[1].split(",")).mapToInt(Integer::parseInt).toArray());}
    }
    public static int radius(String path,int slot){var r=RADII.get(path);return r==null||slot<0||slot>2?0:r[slot];}
    private record Budget(long tick,int used){}
    private static final Map<ServerLevel,Budget> BUDGET=new WeakHashMap<>();
    public static void cast(LivingEntity caster,Catalog.Move move,Vec3 origin){
        if(!(caster.level() instanceof ServerLevel world)||!Settings.DESTRUCTION.get())return;
        var character=Catalog.get(VisualCatalog.path(caster));if(character==null)return;
        int slot=character.moves().indexOf(move),radius=radius(character.id(),slot);if(radius==0)return;
        if(!(caster instanceof ServerPlayer)&&(!Settings.NPC_DESTRUCTION.get()||!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,caster)))return;
        double range=Set.of("strike","rush").contains(move.kind())?4:22;
        Vec3 direction=caster.getLookAngle(),end=world.clip(new ClipContext(origin,origin.add(direction.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster)).getLocation();
        // Purple and cutting attacks carve a path; other moves crater the first impacted surface.
        boolean beam=character.id().equals("gojo")&&slot==2||Set.of("slash","blood","lightning").contains(move.kind());
        Set<BlockPos> visited=new HashSet<>();int removed=0,limit=Settings.DESTRUCTION_LIMIT.get();
        int steps=beam?(int)Math.ceil(origin.distanceTo(end)):0;
        for(int step=0;step<=steps&&removed<limit;step++){
            Vec3 center=steps==0?end:origin.lerp(end,(double)step/steps);
            int width=beam?Math.min(radius,2):radius;
            for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(center).offset(-width,-width,-width),BlockPos.containing(center).offset(width,width,width))){
                if(removed>=limit)break;
                if(pos.distToCenterSqr(center)>width*width+.5||beam&&pos.distToCenterSqr(origin)<9||!visited.add(pos.immutable()))continue;
                if(breakBlock(world,caster,pos))removed++;
            }
        }
    }
    static boolean breakBlock(ServerLevel world,LivingEntity caster,BlockPos pos){
        var budget=BUDGET.get(world);int used=budget!=null&&budget.tick()==world.getGameTime()?budget.used():0;
        if(used>=512||!world.hasChunkAt(pos)||!world.getWorldBorder().isWithinBounds(pos)||world.isOutsideBuildHeight(pos))return false;
        var state=world.getBlockState(pos);float hardness=state.getDestroySpeed(world,pos);
        if(state.isAir()||state.hasBlockEntity()||!state.getFluidState().isEmpty()||hardness<0||hardness>8||state.is(net.minecraft.tags.BlockTags.create(CursedFate.id("terrain_immune"))))return false;
        if(caster instanceof ServerPlayer p){
            if(world.getServer().isUnderSpawnProtection(world,pos,p)||net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world,p.gameMode.getGameModeForPlayer(),p,pos)<0)return false;
        }
        else if(!net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(caster,pos,state))return false;
        if(!world.destroyBlock(pos,false,caster))return false;
        BUDGET.put(world,new Budget(world.getGameTime(),used+1));return true;
    }
}
