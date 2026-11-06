package dev.cursedfate;
import net.minecraftforge.common.ForgeConfigSpec;
public final class Settings {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue PARTICLES,DOMAIN_RADIUS;
    public static final ForgeConfigSpec.BooleanValue PVP;
    static {var b=new ForgeConfigSpec.Builder();
        PARTICLES=b.comment("Maximum particles per technique pulse. Lower for slower computers.").defineInRange("particles",24,0,96);
        DOMAIN_RADIUS=b.defineInRange("domainRadius",12,6,20);
        PVP=b.comment("Whether techniques may damage other players.").define("techniquePvp",false);
        SPEC=b.build();}
}
