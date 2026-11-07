package dev.cursedfate;

import java.util.Set;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Garment depth uses the existing skin UVs; no character PNG is replaced. */
public final class CharacterGeometry {
    public static void tailor(MeshDefinition mesh,String path){
        var root=mesh.getRoot();var body=root.getChild("body");var head=root.getChild("head");
        if(Set.of("gojo","megumi","toge","yuta","maki","mai","miwa","kusakabe","ijichi").contains(path)){
            body.addOrReplaceChild("standing_collar",CubeListBuilder.create().texOffs(16,16).addBox(-4.2f,-1,-2.3f,8.4f,2.5f,4.6f),PartPose.ZERO);
        }
        if(Set.of("geto","kenjaku","sukuna","uraume","yorozu","utahime","gakuganji","tengen","kamo","dhruv").contains(path)){
            body.addOrReplaceChild("robe_overlap",CubeListBuilder.create().texOffs(16,16).addBox(-4.2f,2,-2.35f,8.4f,10,1),PartPose.ZERO);
            root.getChild("right_leg").addOrReplaceChild("robe_right",CubeListBuilder.create().texOffs(0,16).addBox(-2.2f,0,-2.2f,4.4f,7,4.4f),PartPose.ZERO);
            root.getChild("left_leg").addOrReplaceChild("robe_left",CubeListBuilder.create().texOffs(16,48).addBox(-2.2f,0,-2.2f,4.4f,7,4.4f),PartPose.ZERO);
        }
        if(Set.of("nanami","higuruma","ijichi","hara","larue","miguel").contains(path)){
            body.addOrReplaceChild("lapel_left",CubeListBuilder.create().texOffs(16,16).addBox(-3.6f,0,-2.35f,2.3f,6,.5f),PartPose.rotation(0,0,-.12f));
            body.addOrReplaceChild("lapel_right",CubeListBuilder.create().texOffs(16,16).addBox(1.3f,0,-2.35f,2.3f,6,.5f),PartPose.rotation(0,0,.12f));
        }
        if(Set.of("yuji","yuta","choso","eso").contains(path))body.addOrReplaceChild("hood",CubeListBuilder.create().texOffs(16,16).addBox(-4.5f,-.7f,.8f,9,3,2.4f),PartPose.ZERO);
        if(Set.of("panda","todo","rika","finger_bearer","grasshopper").contains(path))body.addOrReplaceChild("shoulders",CubeListBuilder.create().texOffs(16,16).addBox(-5,0,-2.8f,10,5,5.6f),PartPose.ZERO);
        // Character hair is sculpted from the unchanged hair region of its skin atlas.
        if(Set.of("gojo","yuji","megumi","sukuna","kashimo").contains(path)){
            for(int i=0;i<5;i++)head.addOrReplaceChild("hair_tip_"+i,CubeListBuilder.create().texOffs(0,0).addBox(-.8f,-3,-1.4f,1.6f,3,2.8f),PartPose.offsetAndRotation((i-2)*1.7f,-7.5f,(i%2)*1.4f-1,0,0,(i-2)*.13f));
        }
        if(path.equals("choso"))for(int side:new int[]{-1,1})head.addOrReplaceChild("knot"+side,CubeListBuilder.create().texOffs(16,0).addBox(-1.5f,-1.5f,-1.5f,3,3,3),PartPose.offset(side*4,-7,1));
        if(path.equals("hanami"))for(int side:new int[]{-1,1}){
            var branch=head.addOrReplaceChild("branch"+side,CubeListBuilder.create().texOffs(0,0).addBox(-.7f,-7,-.7f,1.4f,7,1.4f),PartPose.offsetAndRotation(side*4,-6,-1,0,0,side*.4f));
            branch.addOrReplaceChild("fork",CubeListBuilder.create().texOffs(0,0).addBox(-.5f,-4,-.5f,1,4,1),PartPose.offsetAndRotation(0,-4,0,0,0,-side*.6f));
        }
    }
}
