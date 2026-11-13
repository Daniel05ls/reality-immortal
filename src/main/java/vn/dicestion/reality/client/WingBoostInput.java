package vn.dicestion.reality.client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import vn.dicestion.reality.RealityImmortal;
import vn.dicestion.reality.WingBoost;

@EventBusSubscriber(modid=RealityImmortal.ID,value=Dist.CLIENT)
public final class WingBoostInput {
    private static boolean sent;
    private static int heartbeat;
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.getConnection()==null) {sent=false;heartbeat=0;return;}
        boolean held=mc.screen==null && mc.player.isFallFlying()
            && mc.player.getItemBySlot(EquipmentSlot.CHEST).is(RealityImmortal.CHEST.get())
            && mc.options.keySprint.isDown();
        if(held!=sent || (held && ++heartbeat>=5)) {
            PacketDistributor.sendToServer(new WingBoost.Input(held));
            sent=held; heartbeat=0;
        }
    }
}
