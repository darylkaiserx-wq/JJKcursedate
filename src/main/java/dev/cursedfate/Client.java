package dev.cursedfate;

import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.lwjgl.glfw.GLFW;
import java.util.*;

@Mod.EventBusSubscriber(modid=CursedFate.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class Client {
    public static CompoundTag state=new CompoundTag();
    public static final Map<java.util.UUID,String> styles=new HashMap<>();
    private static PlayerRenderer styledPlayer;
    private static boolean renderingStyle;
    private static String style(net.minecraft.world.entity.player.Player player){return player==Minecraft.getInstance().player?state.getString("path"):styles.getOrDefault(player.getUUID(),"");}
    @SubscribeEvent public static void playerLayer(EntityRenderersEvent.AddLayers event){
        styledPlayer=new PlayerRenderer(event.getContext(),false){
            @Override public ResourceLocation getTextureLocation(net.minecraft.client.player.AbstractClientPlayer player){var c=Catalog.get(style(player));return c==null?super.getTextureLocation(player):CursedFate.id("textures/entity/"+c.id()+".png");}
        };
    }
    private static final Map<String,KeyMapping> KEYS=new LinkedHashMap<>();
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){
        String[] actions={"cast1","cast2","cast3","domain","rct","guard","reinforce","journal"};
        int[] keys={GLFW.GLFW_KEY_Z,GLFW.GLFW_KEY_X,GLFW.GLFW_KEY_C,GLFW.GLFW_KEY_R,GLFW.GLFW_KEY_V,GLFW.GLFW_KEY_G,GLFW.GLFW_KEY_B,GLFW.GLFW_KEY_K};
        for(int i=0;i<actions.length;i++){var k=new KeyMapping("key.jjkcursedate."+actions[i],InputConstants.Type.KEYSYM,keys[i],"key.categories.jjkcursedate");KEYS.put(actions[i],k);e.register(k);}
    }
    private static net.minecraft.client.model.geom.ModelLayerLocation layer(String id){return new net.minecraft.client.model.geom.ModelLayerLocation(CursedFate.id(id),"main");}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){
        CursedFate.SUMMONS.keySet().forEach(form->e.registerLayerDefinition(ShikigamiModel.layer(form),()->ShikigamiModel.mesh(form)));
        for(var c:Catalog.ALL)e.registerLayerDefinition(layer(c.id()),()->{
            var mesh=PlayerModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,false);
            var head=mesh.getRoot().getChild("head");
            var cubes=net.minecraft.client.model.geom.builders.CubeListBuilder.create();
            switch(c.id()){
                case "jogo" -> cubes.texOffs(0,0).addBox(-3,-12,-3,6,4,6);
                case "hanami" -> cubes.texOffs(0,0).addBox(-6,-10,-1,2,6,2).addBox(4,-10,-1,2,6,2);
                case "dagon" -> cubes.texOffs(0,0).addBox(-5,-9,-5,10,9,10).addBox(-3,-1,-6,2,5,2).addBox(1,-1,-6,2,5,2);
                case "panda" -> cubes.texOffs(0,0).addBox(-5,-10,-2,3,3,3).addBox(2,-10,-2,3,3,3);
                case "gojo","yuji","megumi","sukuna","kashimo" -> cubes.texOffs(0,0).addBox(-4,-10,-3,2,3,6).addBox(-1,-11,-3,2,4,6).addBox(2,-10,-3,2,3,6);
                default -> cubes.texOffs(0,0).addBox(-4,-8,-4,8,1,8,new net.minecraft.client.model.geom.builders.CubeDeformation(.15f));
            }
            head.addOrReplaceChild("silhouette",cubes,net.minecraft.client.model.geom.PartPose.ZERO);
            return net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64);
        });
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){
        for(var entry:CursedFate.SUMMONS.entrySet()){
            String form=entry.getKey();event.registerEntityRenderer(entry.getValue().get(),ctx->new MobRenderer<Shikigami,ShikigamiModel>(ctx,new ShikigamiModel(ctx.bakeLayer(ShikigamiModel.layer(form))),.5f){
                @Override public ResourceLocation getTextureLocation(Shikigami e){return CursedFate.id("textures/entity/summon_"+form+".png");}
            });
        }
        for(var entry:CursedFate.NPCS.entrySet()){
            String path=entry.getKey();
            event.registerEntityRenderer(entry.getValue().get(),ctx->new HumanoidMobRenderer<Sorcerer,PlayerModel<Sorcerer>>(ctx,new PlayerModel<Sorcerer>(ctx.bakeLayer(layer(path)),false){
                @Override public void setupAnim(Sorcerer e,float walk,float amount,float age,float yaw,float pitch){
                    super.setupAnim(e,walk,amount,age,yaw,pitch);
                    if(e.casting()){rightArm.xRot=-1.5f;leftArm.xRot=-1.5f;rightArm.yRot=-.3f;leftArm.yRot=.3f;}
                    if(e.getHealth()<e.getMaxHealth()*.25){body.xRot=.08f;head.xRot+=.12f;}
                    jacket.copyFrom(body);rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);
                }
            },.5f){@Override public ResourceLocation getTextureLocation(Sorcerer e){return CursedFate.id("textures/entity/"+path+".png");}});
        }
    }
    public static void receive(CompoundTag data,boolean open){
        if(data==null)return;state=data;
        var mc=Minecraft.getInstance();if(open)mc.setScreen(new FateScreen());
        else if(mc.screen instanceof FateScreen f&&f.selecting&&!state.getString("path").isEmpty())mc.setScreen(null);
    }
    @Mod.EventBusSubscriber(modid=CursedFate.ID,value=Dist.CLIENT)
    public static class Runtime {
        @SubscribeEvent public static void player(net.minecraftforge.client.event.RenderPlayerEvent.Pre event){
            if(renderingStyle||styledPlayer==null||Catalog.get(style(event.getEntity()))==null)return;
            event.setCanceled(true);renderingStyle=true;
            try{styledPlayer.render((net.minecraft.client.player.AbstractClientPlayer)event.getEntity(),event.getEntity().getYRot(),event.getPartialTick(),event.getPoseStack(),event.getMultiBufferSource(),event.getPackedLight());}finally{renderingStyle=false;}
        }
        @SubscribeEvent public static void arm(net.minecraftforge.client.event.RenderArmEvent event){
            if(styledPlayer==null||Catalog.get(style(event.getPlayer()))==null)return;event.setCanceled(true);
            var model=styledPlayer.getModel();model.attackTime=0;model.crouching=false;model.swimAmount=0;
            model.setupAnim(event.getPlayer(),0,0,0,0,0);
            var c=Catalog.get(style(event.getPlayer()));
            var buffer=event.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(CursedFate.id("textures/entity/"+c.id()+".png")));
            var arm=event.getArm()==net.minecraft.world.entity.HumanoidArm.RIGHT?model.rightArm:model.leftArm;
            var sleeve=event.getArm()==net.minecraft.world.entity.HumanoidArm.RIGHT?model.rightSleeve:model.leftSleeve;
            arm.xRot=0;sleeve.xRot=0;
            arm.render(event.getPoseStack(),buffer,event.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            sleeve.render(event.getPoseStack(),buffer,event.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        }
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
            if(e.phase!=TickEvent.Phase.END||Minecraft.getInstance().player==null)return;
            for(var entry:KEYS.entrySet())while(entry.getValue().consumeClick()){
                String action=entry.getKey();if(action.startsWith("cast"))Net.send("cast",Integer.parseInt(action.substring(4))-1);else Net.send(action,0);
            }
        }
        @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){
            var mc=Minecraft.getInstance();if(e.getOverlay()!=VanillaGuiOverlay.HOTBAR.type()||mc.player==null||mc.options.hideGui)return;
            var c=Catalog.get(state.getString("path"));if(c==null)return;var g=e.getGuiGraphics();int x=12,y=mc.getWindow().getGuiScaledHeight()-80;
            g.fill(x-4,y-5,x+190,y+44,0xb00d101c);g.drawString(mc.font,c.name()+" • "+Progress.rank(state.getInt("rank")),x,y,0xe3d29d);
            g.fill(x,y+14,x+180,y+21,0xff303342);int actualMax=state.getInt("maxEnergy");int max=Math.max(1,actualMax);
            g.fill(x,y+14,x+(int)(180*Math.max(0,Math.min(1,state.getFloat("energy")/max))),y+21,0xff69bce9);
            g.drawString(mc.font,(actualMax==0?"Heavenly Restriction":"CE "+(int)state.getFloat("energy")+" / "+max)+"    Lv. "+state.getInt("level"),x,y+26,0xffd5e8f5);
        }
        @SubscribeEvent public static void hand(RenderHandEvent e){
            var mc=Minecraft.getInstance();if(mc.player==null)return;
            long since=mc.player.level().getGameTime()-state.getLong("lastCast");
            if(state.contains("lastCast")&&since>=0&&since<12){float p=(float)Math.sin((since+e.getPartialTick())/12*Math.PI);e.getPoseStack().translate(0,p*.12,-p*.16);e.getPoseStack().mulPose(Axis.XP.rotationDegrees(-p*18));}
        }
        @SubscribeEvent public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e){state=new CompoundTag();styles.clear();}
    }
    public static final class FateScreen extends Screen {
        private EditBox search;
        private int page=0;
        private int detailScroll=0;
        private String filter="all";
        private boolean selecting;
        private String selected="gojo";
        public FateScreen(){super(Component.literal("The Book of Cursed Fate"));selecting=state.getString("path").isEmpty();if(!selecting)selected=state.getString("path");}
        private int left(){return (width- Math.min(580,width-20))/2;}
        private int panel(){return Math.min(580,width-20);}
        @Override protected void init(){
            clearWidgets();rows.clear();actionButton=null;missionButton=null;int x=left(),w=panel();int top=42;
            search=new EditBox(font,x+12,top,w/2-22,18,Component.literal("Search characters"));search.setHint(Component.literal("Search characters…"));
            search.setResponder(s->{page=0;rebuildRows();});addRenderableWidget(search);
            for(int i=0;i<3;i++){String role=new String[]{"all","allies","curses"}[i];addRenderableWidget(Button.builder(Component.literal(role),b->{filter=role;page=0;rebuildRows();}).bounds(x+12+i*62,top+24,60,18).build());}
            addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);rebuildRows();}).bounds(x+12,height-35,35,20).build());
            addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;rebuildRows();}).bounds(x+52,height-35,35,20).build());
            addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(x+w-72,height-35,60,20).build());
            rebuildRows();
        }
        private final List<Button> rows=new ArrayList<>();
        private int rowCount(){return Math.max(1,(height-150)/24);}
        private List<Catalog.Character> matches(){String query=search==null?"":search.getValue().toLowerCase(Locale.ROOT);return Catalog.ALL.stream().filter(c->c.name().toLowerCase(Locale.ROOT).contains(query)).filter(c->filter.equals("all")||filter.equals("curses")==c.hostile()).toList();}
        private void rebuildRows(){
            rows.forEach(this::removeWidget);rows.clear();var list=matches();int n=rowCount();page=Math.max(0,Math.min(page,Math.max(0,(list.size()-1)/n)));
            for(int i=0;i<n&&page*n+i<list.size();i++){var c=list.get(page*n+i);var b=Button.builder(Component.literal(c.name()),btn->{selected=c.id();detailScroll=0;}).bounds(left()+12,94+i*24,panel()/2-22,21).build();rows.add(b);addRenderableWidget(b);}
            // Detail action buttons are created once separately by render-free layout.
            if(actionButton!=null)removeWidget(actionButton);
            actionButton=Button.builder(Component.literal(selecting?"Choose character":"Promotion trial"),b->{if(selecting){Net.send("choose:"+selected,0);}else Net.send("promote",0);}).bounds(left()+panel()/2+12,height-65,panel()/2-24,22).build();addRenderableWidget(actionButton);
            if(!selecting&&missionButton==null){missionButton=Button.builder(Component.literal("Claim mission"),b->Net.send("mission",0)).bounds(left()+panel()/2+12,height-92,panel()/2-24,22).build();addRenderableWidget(missionButton);}
        }
        private Button actionButton,missionButton;
        @Override public boolean mouseScrolled(double x,double y,double delta){if(x>left()+panel()/2){detailScroll=Math.max(0,Math.min(240,detailScroll+(delta<0?20:-20)));}else {page+=delta<0?1:-1;rebuildRows();}return true;}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            renderBackground(g);int x=left(),w=panel(),dx=x+w/2+12;g.fill(x,12,x+w,height-10,0xf0101523);g.fill(x,12,x+w,15,0xffcbb77b);
            g.drawString(font,"CURSED FATE  /  CHARACTER ARCHIVE",x+12,24,0xffe4d4a0);
            var c=Catalog.get(selected);int y=48-detailScroll;
            g.enableScissor(dx,42,x+w-8,height-(selecting?78:100));
            if(c!=null){
                g.drawString(font,c.name(),dx,y,0xff000000|c.tint());y+=17;
                g.drawString(font,c.role().toUpperCase(Locale.ROOT),dx,y,0xff959eb1);y+=24;
                for(int i=0;i<c.moves().size();i++){var m=c.moves().get(i);g.drawWordWrap(font,Component.literal(new String[]{"Z","X","C"}[i]+" • "+m.name()),dx,y,w/2-25,0xffe0e3ea);y+=22;g.drawString(font,"Lv "+m.level()+" / CE "+m.cost()+(c.id().equals(state.getString("path"))?" / M"+Progress.mastery(state.getInt("mastery"+i)):""),dx,y,0xff7d9eb9);y+=20;}
                g.drawWordWrap(font,Component.literal(c.domain().isEmpty()?"No innate domain. Simple Domain: G":"R • "+c.domain()+" (Lv 30)"),dx,y,w/2-25,0xffb7a0dc);
                if(height>340){y+=36;g.drawWordWrap(font,Component.literal("V: Reverse Cursed Technique (Lv 24)\nB: Reinforcement • G: Simple Domain\nK: Journal • Z/X/C: Techniques"),dx,y,w/2-25,0xffa9b5c8);}
            }
            g.disableScissor();
            g.drawString(font,"Scroll here for details",dx,height-(selecting?76:98),0xff8b94aa);
            g.drawString(font,"Page "+(page+1)+" • "+matches().size()+" characters",x+96,height-29,0xffa7afbf);
            super.render(g,mx,my,partial);
        }
        @Override public boolean isPauseScreen(){return false;}
    }
}
