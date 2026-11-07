package dev.cursedfate;
import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public final class SpriteEffect extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final String kind;private final float size;
    private SpriteEffect(ClientLevel world,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites,String kind){
        super(world,x,y,z,vx,vy,vz);this.sprites=sprites;this.kind=kind;this.lifetime=kind.equals("black_flash")?12:18;this.quadSize=kind.equals("black_flash")?.9f:kind.equals("slash")?.55f:.3f;this.size=this.quadSize;this.hasPhysics=false;this.gravity=0;setSpriteFromAge(sprites);
    }
    @Override public void tick(){super.tick();setSpriteFromAge(sprites);float t=(float)age/lifetime;alpha=(1-t)*(1-t);quadSize=size*(kind.equals("slash")?.7f+t:kind.equals("fire")?1-t*.5f:1+t*.25f);}
    @Override protected int getLightColor(float partial){return 0xf000f0;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites,String kind){return (type,world,x,y,z,vx,vy,vz)->new SpriteEffect(world,x,y,z,vx,vy,vz,sprites,kind);}
}
