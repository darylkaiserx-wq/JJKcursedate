package dev.cursedfate;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Eight sculpted silhouettes with articulated wings, paws, tendrils and adaptation wheel. */
public final class ShikigamiModel extends HierarchicalModel<Shikigami> {
    private final ModelPart root,head,left,right,wingL,wingR;
    public ShikigamiModel(ModelPart root){this.root=root;head=root.getChild("head");left=root.getChild("left");right=root.getChild("right");wingL=root.getChild("wing_l");wingR=root.getChild("wing_r");}
    public static ModelLayerLocation layer(String form){return new ModelLayerLocation(CursedFate.id("summon_"+form),"main");}
    private static PartDefinition part(PartDefinition parent,String name,float x,float y,float z,float w,float h,float d,float px,float py,float pz,int u,int v){return parent.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d),PartPose.offset(px,py,pz));}
    public static LayerDefinition mesh(String form){
        var mesh=new MeshDefinition();var r=mesh.getRoot();
        // Empty common pivots keep animation code total for every shape.
        for(String id:new String[]{"body","head","left","right","wing_l","wing_r"})r.addOrReplaceChild(id,CubeListBuilder.create(),PartPose.ZERO);
        switch(form){
            case "divine_dog"->{
                part(r,"body",-4,-4,-7,8,8,15,0,13,0,0,24);var h=part(r,"head",-3.5f,-5,-5,7,7,7,0,10,-7,0,0);
                part(h,"muzzle",-2.5f,-1,-8,5,3,4,0,0,0,32,8);part(h,"ear_l",-3.5f,-9,-1,2,5,3,0,0,0,32,0);part(h,"ear_r",1.5f,-9,-1,2,5,3,0,0,0,32,0);
                for(String id:new String[]{"left","right","wing_l","wing_r"}){boolean front=id.startsWith("wing");float x=id.equals("left")||id.equals("wing_l")?3:-3;var leg=part(r,id,-1.5f,0,-1.5f,3,8,3,x,15,front?-5:5,32,16);part(leg,"paw",-1.8f,6,-2.7f,3.6f,3,4.2f,0,0,0,32,30);}
                var tail=part(r,"tail",-1,-1,0,2,2,8,0,11,7,0,48);
            }
            case "nue"->{
                part(r,"body",-4,-5,-3,8,10,6,0,13,0,0,24);var h=part(r,"head",-3,-4,-3,6,7,6,0,6,0,0,0);part(h,"beak",-1.2f,-.5f,-6,2.4f,2,4,0,0,0,32,8);
                for(int side:new int[]{-1,1}){
                    var wing=part(r,side>0?"wing_l":"wing_r",side>0?0:-10,-1,-2,10,2,5,side*3,10,0,0,48);
                    var tip=part(wing,"tip",side>0?0:-10,-1,-2,10,2,4,side*9,0,0,0,48);
                    for(int i=0;i<5;i++)part(tip,"feather"+i,side>0?i*1.7f:-i*1.7f-2,0,0,2,1,7-i*.7f,0,0,0,32,40);
                    var leg=part(r,side>0?"left":"right",-1,0,-1,2,6,2,side*2,17,0,32,16);for(int i=0;i<3;i++)part(leg,"talon"+i,-1+i*.8f,5,-3, .6f,1,3,0,0,0,32,8);
                }
                for(int i=0;i<3;i++)part(r,"tail"+i,-1,0,0,2,2,8, (i-1)*2,16,2,0,48);
            }
            case "moon_dregs"->{
                var h=part(r,"head",-6,-3,-6,12,4,12,0,6,0,0,0);part(h,"crown",-4,-5,-4,8,2,8,0,0,0,0,0);part(h,"rim",-7,0,-7,14,2,14,0,0,0,0,24);
                for(int i=0;i<8;i++){double a=i*Math.PI/4;var t=part(r,i==0?"left":i==1?"right":i==2?"wing_l":i==3?"wing_r":"tendril"+i,-.6f,0,-.6f,1.2f,8,1.2f,(float)Math.cos(a)*4,8,(float)Math.sin(a)*4,32,16);part(t,"tip",-.4f,0,-.4f,.8f,7,.8f,0,7,0,32,16);}
            }
            case "garuda"->{
                var h=part(r,"head",-2.5f,-2.5f,-4,5,5,6,0,10,-7,0,0);part(h,"snout",-1.5f,-.5f,-6,3,2,3,0,0,0,32,8);
                var parent=r;
                for(int i=0;i<7;i++){var segment=part(parent,i==0?"body":"segment"+i,-2,-2,0,4,4,5,0,i==0?10:0,i==0?-4:4,0,24);part(segment,"ridge",-.5f,-3,1,1,1,3,0,0,0,32,0);parent=segment;}
                for(int side:new int[]{-1,1}){var wing=part(r,side>0?"wing_l":"wing_r",side>0?0:-8,-.5f,-2,8,1,5,side*2,10,-1,0,48);for(int j=0;j<3;j++)part(wing,"feather"+j,side>0?j*2:-j*2-2,0,1,2,1,6-j,0,0,0,32,40);}
            }
            case "death_swarm"->{
                part(r,"body",-2,-2,-3,4,4,6,0,13,0,0,24);part(r,"head",-2,-2,-2,4,4,4,0,12,-4,0,0);
                for(int i=0;i<8;i++){double a=i*Math.PI/4;var bug=part(r,"swarm"+i,-1,-1,-2,2,2,4,(float)Math.sin(a)*7,10+(i%3)*2,(float)Math.cos(a)*7,32,16);part(bug,"wings",-3,-.2f,-1,6,.4f,3,0,0,0,0,48);}
                for(int side:new int[]{-1,1})part(r,side>0?"wing_l":"wing_r",side>0?0:-6,-.3f,-2,6,.6f,5,side*2,12,0,0,48);
            }
            default->{
                boolean wheel=form.equals("mahoraga"),queen=form.equals("rika_manifestation");
                part(r,"body",-5,-7,-3,10,16,6,0,4,0,0,24);part(r,"chest",-6,-3,-3.5f,12,5,7,0,1,0,0,24);var h=part(r,"head",-4,-6,-4,8,7,8,0,-5,0,0,0);
                for(int side:new int[]{-1,1}){
                    var arm=part(r,side>0?"wing_l":"wing_r",-2,0,-2,4,12,4,side*7,-1,0,0,48);var fore=part(arm,"forearm",-1.6f,0,-1.6f,3.2f,8,3.2f,0,10,0,32,16);
                    for(int i=0;i<4;i++)part(fore,"claw"+i,-1.5f+i*.9f,7,-2,.6f,3,1,0,0,0,32,8);
                    part(r,side>0?"left":"right",-2,0,-2,4,11,4,side*3,13,0,32,16);
                    if(queen){part(h,side>0?"horn_l":"horn_r",-1,-7,-1,2,7,2,side*4,-3,0,32,0);for(int i=0;i<3;i++)part(h,"tooth"+side+"_"+i,side*(i+1)-.4f,0,-4.5f,.8f,2,1,0,0,0,32,8);}
                }
                if(wheel){
                    var w=h.addOrReplaceChild("wheel",CubeListBuilder.create(),PartPose.offset(0,-10,1));
                    for(int i=0;i<8;i++){float a=i*Mth.PI/4;var spoke=part(w,"spoke"+i,-.35f,-7,-.35f,.7f,7,.7f,0,0,0,48,0);spoke= w.addOrReplaceChild("spoke"+i,CubeListBuilder.create().texOffs(48,0).addBox(-.35f,-7,-.35f,.7f,7,.7f).addBox(-1.3f,-8,-1,2.6f,2,2),PartPose.rotation(0,0,a));}
                    part(w,"hub",-1.3f,-1.3f,-1.3f,2.6f,2.6f,2.6f,0,0,0,48,0);
                }else if(!queen){for(int i=0;i<3;i++)part(h,"horn"+i,-.7f,-4,-.7f,1.4f,4,1.4f,(i-1)*3,-5,0,32,0);}
            }
        }
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(Shikigami e,float walk,float amount,float age,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);
        head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;float stride=Mth.cos(walk*.7f)*amount;
        left.xRot=stride;right.xRot=-stride;
        switch(e.form){
            case "nue"->{wingL.zRot=Mth.sin(age*.45f)*.65f+.15f;wingR.zRot=-wingL.zRot;wingL.getChild("tip").zRot=Mth.sin(age*.45f-.5f)*.35f;wingR.getChild("tip").zRot=-wingL.getChild("tip").zRot;root.y=Mth.sin(age*.2f)*.4f;}
            case "divine_dog"->{wingL.xRot=-stride;wingR.xRot=stride;root.getChild("tail").yRot=Mth.sin(age*.2f)*.3f;root.getChild("tail").xRot=-.6f;head.xRot+=Mth.sin(age*.1f)*.04f;}
            case "moon_dregs"->{root.y=Mth.sin(age*.12f);for(var part:root.getAllParts().toList())if(part.hasChild("tip")){part.xRot=Mth.sin(age*.12f+part.x)*.18f;part.zRot=Mth.cos(age*.1f+part.z)*.18f;part.getChild("tip").xRot=Mth.sin(age*.13f+part.x)*.3f;}}
            case "garuda"->{var segment=root.getChild("body");for(int i=1;i<7;i++){segment=segment.getChild("segment"+i);segment.yRot=Mth.sin(age*.18f-i*.5f)*.22f;segment.xRot=Mth.cos(age*.12f-i*.5f)*.1f;}wingL.zRot=Mth.sin(age*.3f)*.3f;wingR.zRot=-wingL.zRot;}
            case "death_swarm"->{wingL.zRot=Mth.sin(age*1.7f)*.7f;wingR.zRot=-wingL.zRot;for(int i=0;i<8;i++){var bug=root.getChild("swarm"+i);bug.y+=Mth.sin(age*.25f+i)*1.5f;bug.yRot=age*.08f+i;bug.getChild("wings").zRot=Mth.sin(age*1.7f+i)*.5f;}}
            default->{wingL.xRot=-stride*.7f;wingR.xRot=stride*.7f;float attack=Mth.sin(e.getAttackAnim(age-e.tickCount)*Mth.PI);wingL.xRot-=attack*1.4f;wingR.xRot-=attack*1.4f;wingL.getChild("forearm").xRot=-.16f-attack*.5f;wingR.getChild("forearm").xRot=-.16f-attack*.5f;if(head.hasChild("wheel"))head.getChild("wheel").zRot=(age*.018f);}
        }
    }
}
