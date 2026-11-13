package vn.dicestion.reality.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import vn.dicestion.reality.RealityImmortal;

@EventBusSubscriber(modid=RealityImmortal.ID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class RealityClient {
    private static final ModelLayerLocation OUTER = layer("god_outer");
    private static final ModelLayerLocation LEGS = layer("god_legs");
    private static final ModelLayerLocation BOOTS = layer("god_boots");
    private static GodArmorModel outer, legs, boots;
    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RealityImmortal.ID,name),"main");
    }
    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OUTER, () -> GodArmorModel.layer(false,false));
        event.registerLayerDefinition(LEGS, () -> GodArmorModel.layer(true,false));
        event.registerLayerDefinition(BOOTS, () -> GodArmorModel.layer(false,true));
    }
    @SubscribeEvent
    public static void reload(EntityRenderersEvent.AddLayers event) { outer=null;legs=null;boots=null; }
    @SubscribeEvent
    public static void extensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                var models=Minecraft.getInstance().getEntityModels();
                if(slot==EquipmentSlot.LEGS) {
                    if(legs==null) legs=new GodArmorModel(models.bakeLayer(LEGS));
                    return legs;
                }
                if(slot==EquipmentSlot.FEET) {
                    if(boots==null) boots=new GodArmorModel(models.bakeLayer(BOOTS));
                    return boots;
                }
                if(outer==null) outer=new GodArmorModel(models.bakeLayer(OUTER));
                outer.animateWings(entity,slot,Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
                return outer;
            }
        },RealityImmortal.HELMET.get(),RealityImmortal.CHEST.get(),RealityImmortal.LEGS.get(),RealityImmortal.BOOTS.get());
        event.registerItem(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                    ItemStack stack, float partial, float equip, float swing) {
                float sweep=(float)Math.sin(Math.sqrt(swing)*Math.PI);
                float sign=arm==HumanoidArm.RIGHT?1:-1;
                pose.mulPose(Axis.ZP.rotationDegrees(sign * -25F * sweep));
                pose.mulPose(Axis.YP.rotationDegrees(sign * 18F * sweep));
                pose.translate(0,0,-0.18F*sweep);
                return false;
            }
        },RealityImmortal.SWORD.get());
    }
}
