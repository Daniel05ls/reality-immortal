package vn.dicestion.reality.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;

/** Layered, articulated geometry using the vanilla humanoid pose and armor slot masks. */
public final class GodArmorModel extends HumanoidModel<LivingEntity> {
    public GodArmorModel(ModelPart root) { super(root); }
    private static CubeListBuilder gold() { return CubeListBuilder.create().texOffs(64,0); }
    private static CubeListBuilder crystal() { return CubeListBuilder.create().texOffs(96,0); }
    private static CubeListBuilder ivory() { return CubeListBuilder.create().texOffs(64,32); }
    private static CubeListBuilder violet() { return CubeListBuilder.create().texOffs(96,32); }
    private static CubeListBuilder dark() { return CubeListBuilder.create().texOffs(64,64); }

    public void animateWings(LivingEntity entity, EquipmentSlot slot, float partial) {
        if (slot != EquipmentSlot.CHEST) return;
        boolean flying = entity.isFallFlying() || (entity instanceof net.minecraft.world.entity.player.Player p && p.getAbilities().flying);
        float spread = flying ? 0.04F : 0.25F;
        float flutter = (float)Math.sin((entity.tickCount + partial) * (flying ? 0.32F : 0.07F)) * (flying ? 0.28F : 0.07F);
        body.getChild("angel_left").yRot = spread + flutter;
        body.getChild("angel_right").yRot = -spread - flutter;
    }

    public static LayerDefinition layer(boolean leggings, boolean boots) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(leggings ? 0.45F : 0.9F), 0);
        PartDefinition root = mesh.getRoot();
        if (boots) {
            root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F,12,0));
            root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F,12,0));
        }
        PartDefinition head = root.getChild("head"), body = root.getChild("body");
        if (!leggings && !boots) {
            // Feathered wings attach to the torso and inherit crouch/body motion.
            for (int side : new int[]{-1,1}) {
                PartDefinition wing = body.addOrReplaceChild(side < 0 ? "angel_left" : "angel_right",
                    gold().addBox(-1,-1,-0.7F,2,3,1.4F),
                    PartPose.offset(side*2.8F,2,3.8F));
                for (int i=0;i<9;i++) {
                    float reach = 2F + i*1.55F;
                    float rise = -1.8F - (float)Math.sin(i/8F*Math.PI)*5F;
                    float length = 5.5F + i*0.9F;
                    wing.addOrReplaceChild("feather_"+i,
                        ivory().addBox(-1.0F,0,-0.45F,2F,length,0.9F),
                        PartPose.offsetAndRotation(side*reach,rise,0.3F+i*0.16F,0.08F,0,-side*(0.25F+i*0.065F)));
                    wing.addOrReplaceChild("quill_"+i,
                        gold().addBox(-0.17F,0,-0.65F,0.34F,length*0.8F,0.35F),
                        PartPose.offsetAndRotation(side*reach,rise,0.3F+i*0.16F,0.08F,0,-side*(0.25F+i*0.065F)));
                    wing.addOrReplaceChild("covert_"+i,
                        ivory().addBox(-1.2F,0,-0.9F,2.4F,3.8F,1.0F),
                        PartPose.offsetAndRotation(side*reach,rise-0.2F,-0.15F+i*0.16F,0,0,-side*0.25F));
                }
            }
            // Raised brow, crystal visor and five crown points.
            head.addOrReplaceChild("brow", gold().addBox(-4.5F,-8.5F,-5.15F,9,1.2F,1.1F), PartPose.ZERO);
            head.addOrReplaceChild("visor", crystal().addBox(-3.5F,-5.9F,-5.1F,7,0.8F,0.65F), PartPose.ZERO);
            head.addOrReplaceChild("crown_gem", crystal().addBox(-0.85F,-10.3F,-5.4F,1.7F,2.5F,1.4F), PartPose.ZERO);
            for (int i=-2;i<=2;i++) {
                float h= i==0 ? 4F : (Math.abs(i)==1?3.2F:2.3F);
                head.addOrReplaceChild("crown_"+i, gold().addBox(-0.45F,-h,0,0.9F,h,1),
                    PartPose.offsetAndRotation(i*1.9F,-8.5F,-4.7F,0,0,i*0.13F));
            }
            for(int side:new int[]{-1,1}) {
                head.addOrReplaceChild("temple_"+side, ivory().addBox(-0.7F,-2,-2,1.4F,5,4),
                    PartPose.offsetAndRotation(side*4.7F,-5,0,0,0,-side*0.15F));
                head.addOrReplaceChild("horn_lower_"+side, gold().addBox(-0.6F,-4,0,1.2F,4,1.2F),
                    PartPose.offsetAndRotation(side*4.4F,-7,0,0.2F,0,side*0.4F));
                head.addOrReplaceChild("horn_tip_"+side, ivory().addBox(-0.4F,-3,0,0.8F,3,0.8F),
                    PartPose.offsetAndRotation(side*5.8F,-10.5F,0.7F,-0.2F,0,-side*0.25F));
            }
            // Eight small bars form a real hovering crown-ring behind the head.
            for(int i=0;i<8;i++) {
                float a=(float)(i*Math.PI/4);
                head.addOrReplaceChild("halo_"+i, gold().addBox(-2.25F,-0.35F,-0.35F,4.5F,0.7F,0.7F),
                    PartPose.offsetAndRotation((float)Math.cos(a)*5.4F,-6+(float)Math.sin(a)*5.4F,5.4F,0,0,a+(float)Math.PI/2));
            }
            body.addOrReplaceChild("breastplate", ivory().addBox(-4.4F,1,-3.35F,8.8F,5,1.5F),PartPose.ZERO);
            body.addOrReplaceChild("core_frame", gold().addBox(-1.8F,-1.8F,-0.6F,3.6F,3.6F,1.2F),
                PartPose.offsetAndRotation(0,4,-4,0,0,(float)Math.PI/4));
            body.addOrReplaceChild("core", crystal().addBox(-1.1F,-1.1F,-0.75F,2.2F,2.2F,1.5F),
                PartPose.offsetAndRotation(0,4,-4.5F,0,0,(float)Math.PI/4));
            for(int i=0;i<3;i++) body.addOrReplaceChild("abdomen_"+i,
                dark().addBox(-3.6F,0,-3.2F,7.2F,1.5F,1),PartPose.offset(0,6.6F+i*1.6F,0));
            body.addOrReplaceChild("belt",gold().addBox(-4.9F,10.4F,-3.4F,9.8F,1.1F,6.8F),PartPose.ZERO);
            body.addOrReplaceChild("back_spine",gold().addBox(-0.6F,1,3,1.2F,9,1.1F),PartPose.ZERO);
            for(int side:new int[]{-1,1}) {
                PartDefinition arm=root.getChild(side==-1?"right_arm":"left_arm");
                float center=side==-1?-1:1;
                arm.addOrReplaceChild("pauldron",ivory().addBox(-3,-2,-3.3F,6,3.2F,6.6F),PartPose.offset(center,-1,0));
                arm.addOrReplaceChild("rim",gold().addBox(-3.4F,-0.3F,-3.6F,6.8F,0.8F,7.2F),PartPose.offset(center,-1,0));
                for(int i=0;i<3;i++) arm.addOrReplaceChild("wing_fin_"+i,
                    ivory().addBox(-0.5F,-3.8F,0,1,4.5F,3.8F),
                    PartPose.offsetAndRotation(center+side*(1.2F+i*0.9F),-1-i*0.7F,1+i*0.8F,0.3F,0,side*(0.3F+i*0.12F)));
                arm.addOrReplaceChild("gauntlet",dark().addBox(-2.6F,5,-2.7F,5.2F,4.7F,5.4F),PartPose.offset(center,0,0));
                arm.addOrReplaceChild("gauntlet_rim",gold().addBox(-2.8F,8.8F,-2.9F,5.6F,0.8F,5.8F),PartPose.offset(center,0,0));
                arm.addOrReplaceChild("rune",crystal().addBox(-0.5F,5.5F,-3,1,2.8F,0.5F),PartPose.offset(center,0,0));
            }
        }
        for(String name:new String[]{"right_leg","left_leg"}) {
            PartDefinition leg=root.getChild(name);
            if (leggings) {
                leg.addOrReplaceChild("thigh_plate",ivory().addBox(-2.5F,0.5F,-2.9F,5,4,1.3F),PartPose.ZERO);
                leg.addOrReplaceChild("knee_frame",gold().addBox(-1.7F,-1.7F,-0.6F,3.4F,3.4F,1.2F),
                    PartPose.offsetAndRotation(0,5.8F,-2.7F,0,0,(float)Math.PI/4));
                leg.addOrReplaceChild("knee_core",violet().addBox(-0.9F,4.8F,-3.7F,1.8F,1.8F,0.8F),PartPose.ZERO);
            } else if (boots) {
                leg.addOrReplaceChild("greave",dark().addBox(-2.6F,6.6F,-2.9F,5.2F,5.4F,5.8F),PartPose.ZERO);
                leg.addOrReplaceChild("toe",ivory().addBox(-2.7F,10.1F,-4.5F,5.4F,2.1F,7.4F),PartPose.ZERO);
                leg.addOrReplaceChild("sole",gold().addBox(-2.8F,11.7F,-4.6F,5.6F,0.7F,7.6F),PartPose.ZERO);
                leg.addOrReplaceChild("shin_rune",crystal().addBox(-0.45F,7.1F,-3.3F,0.9F,3.1F,0.7F),PartPose.ZERO);
            }
        }
        return LayerDefinition.create(mesh,128,128);
    }
}
