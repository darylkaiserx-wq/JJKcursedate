package dev.cursedfate;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class ShikigamiModel extends HierarchicalModel<Shikigami> {
    private final ModelPart root,head,left,right,wingL,wingR;
    public ShikigamiModel(ModelPart root){this.root=root;head=root.getChild("head");left=root.getChild("left");right=root.getChild("right");wingL=root.getChild("wing_l");wingR=root.getChild("wing_r");}
    public static ModelLayerLocation layer(String form){return new ModelLayerLocation(CursedFate.id("summon_"+form),"main");}
    public static LayerDefinition mesh(String form){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        boolean giant=form.equals("mahoraga")||form.equals("rika_manifestation");
        boolean bird=form.equals("nue");boolean dog=form.equals("divine_dog");
        root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,24).addBox(-5,-8,-3,10,giant?18:8,6),PartPose.offset(0,giant?4:14,0));
        var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-6,-4,8,6,8),PartPose.offset(0,giant?-4:12,dog?-5:0));
        if(dog){head.addOrReplaceChild("ears",CubeListBuilder.create().texOffs(32,0).addBox(-4,-10,-2,2,5,2).addBox(2,-10,-2,2,5,2),PartPose.ZERO);head.addOrReplaceChild("muzzle",CubeListBuilder.create().texOffs(32,8).addBox(-2,-3,-7,4,3,4),PartPose.ZERO);}
        if(bird)head.addOrReplaceChild("beak",CubeListBuilder.create().texOffs(32,8).addBox(-1,-3,-7,2,2,4),PartPose.ZERO);
        root.addOrReplaceChild("left",CubeListBuilder.create().texOffs(32,16).addBox(-2,0,-2,4,giant?12:8,4),PartPose.offset(3,giant?12:16,0));
        root.addOrReplaceChild("right",CubeListBuilder.create().texOffs(32,16).addBox(-2,0,-2,4,giant?12:8,4),PartPose.offset(-3,giant?12:16,0));
        var l=CubeListBuilder.create().texOffs(0,48);var r=CubeListBuilder.create().texOffs(0,48);
        if(bird){l.addBox(0,-1,-3,16,2,6);r.addBox(-16,-1,-3,16,2,6);}
        else if(giant){l.addBox(0,0,-2,4,14,4);r.addBox(-4,0,-2,4,14,4);}
        else{l.addBox(0,0,-2,2,8,2);r.addBox(-2,0,-2,2,8,2);}
        root.addOrReplaceChild("wing_l",l,PartPose.offset(giant?5:4,giant?0:13,0));root.addOrReplaceChild("wing_r",r,PartPose.offset(giant?-5:-4,giant?0:13,0));
        if(form.equals("mahoraga")){
            var wheel=CubeListBuilder.create().texOffs(48,0);
            for(int i=0;i<12;i++){double a=i*Math.PI/6;wheel.addBox((float)Math.cos(a)*7-1,(float)Math.sin(a)*7-17,-1,2,2,2);}
            head.addOrReplaceChild("wheel",wheel,PartPose.ZERO);
        }
        if(form.equals("moon_dregs")){head.addOrReplaceChild("bell",CubeListBuilder.create().texOffs(0,0).addBox(-6,-7,-6,12,4,12),PartPose.ZERO);}
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(Shikigami e,float limbSwing,float amount,float age,float yaw,float pitch){
        head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;
        left.xRot=Mth.cos(limbSwing*.7f)*amount;right.xRot=-left.xRot;
        if(e.form.equals("nue")){wingL.zRot=Mth.sin(age*.5f)*.8f;wingR.zRot=-wingL.zRot;}
        else{wingL.xRot=Mth.cos(limbSwing*.7f+Mth.PI)*amount;wingR.xRot=-wingL.xRot;}
    }
}
