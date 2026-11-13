package vn.dicestion.reality.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import vn.dicestion.reality.RealityImmortal;

@EventBusSubscriber(modid=RealityImmortal.ID,value=Dist.CLIENT)
public final class AngelFlightPose {
    private static final Set<PoseStack> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());
    private AngelFlightPose() {}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void before(RenderPlayerEvent.Pre event) {
        var p=event.getEntity();
        if (!p.getAbilities().flying || p.isFallFlying() || p.isPassenger()
            || !p.getItemBySlot(EquipmentSlot.CHEST).is(RealityImmortal.CHEST.get())) return;
        PoseStack pose=event.getPoseStack();
        pose.pushPose();
        ACTIVE.add(pose);
        float yaw=180F-Mth.rotLerp(event.getPartialTick(),p.yBodyRotO,p.yBodyRot);
        float pitch=Mth.lerp(event.getPartialTick(),p.xRotO,p.getXRot());
        // Same nose-down body orientation as Elytra; purely visual, no flight physics changes.
        pose.translate(0,1.05,0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(-90F-pitch));
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(0,-1.05,0);
    }
    @SubscribeEvent
    public static void after(RenderPlayerEvent.Post event) {
        if(ACTIVE.remove(event.getPoseStack())) event.getPoseStack().popPose();
    }
}
