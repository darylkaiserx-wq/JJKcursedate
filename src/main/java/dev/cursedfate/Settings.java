package dev.cursedfate;
import net.minecraftforge.common.ForgeConfigSpec;
public final class Settings {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue PARTICLES,DOMAIN_RADIUS,DESTRUCTION_LIMIT;
    public static final ForgeConfigSpec.BooleanValue PVP,CAMERA_MOTION,DESTRUCTION,NPC_DESTRUCTION,DOMAIN_BLOCKS;
    static {var b=new ForgeConfigSpec.Builder();
        PARTICLES=b.comment("Maximum particles per technique pulse. Lower for slower computers.").defineInRange("particles",24,0,96);
        DOMAIN_RADIUS=b.defineInRange("domainRadius",12,6,20);
        CAMERA_MOTION=b.comment("Local first-person camera recoil; disable for reduced motion.").define("cameraMotion",true);
        DESTRUCTION=b.comment("Allow explicitly destructive moves to break terrain permanently.").define("techniqueDestruction",true);
        NPC_DESTRUCTION=b.comment("NPC terrain damage also respects mobGriefing.").define("npcDestruction",true);
        DOMAIN_BLOCKS=b.define("domainBlocks",true);
        DESTRUCTION_LIMIT=b.defineInRange("destructionBlocksPerCast",128,1,256);
        PVP=b.comment("Whether techniques may damage other players.").define("techniquePvp",false);
        SPEC=b.build();}
}
