package vn.dicestion.reality;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class WingBoost {
    private static final String UNTIL="realityimmortal:boost_until";
    private WingBoost() {}
    public record Input(boolean held) implements CustomPacketPayload {
        public static final Type<Input> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(RealityImmortal.ID,"wing_boost"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Input> CODEC=new StreamCodec<>() {
            public Input decode(RegistryFriendlyByteBuf buf) { return new Input(buf.readBoolean()); }
            public void encode(RegistryFriendlyByteBuf buf,Input value) { buf.writeBoolean(value.held()); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(Input.TYPE,Input.CODEC,(input,context) ->
            context.enqueueWork(() -> {
                if(context.player() instanceof ServerPlayer p) {
                    p.getPersistentData().putLong(UNTIL,input.held() ? p.level().getGameTime()+15 : -1);
                }
            }));
    }
    public static void tick(ServerPlayer p) {
        if (!p.isAlive() || !p.isFallFlying()
            || !p.getItemBySlot(EquipmentSlot.CHEST).is(RealityImmortal.CHEST.get())) {
            p.getPersistentData().remove(UNTIL); return;
        }
        var data=p.getPersistentData();
        if (!data.contains(UNTIL) || data.getLong(UNTIL)<p.level().getGameTime()) return;
        Vec3 motion=p.getDeltaMovement();
        Vec3 look=p.getLookAngle();
        // Smooth firework-like acceleration with bounded speed.
        Vec3 velocity=motion.add(look.scale(0.12)).add(look.scale(2.2).subtract(motion).scale(0.035));
        if(velocity.lengthSqr()>9) velocity=velocity.normalize().scale(3);
        p.setDeltaMovement(velocity);
        p.hurtMarked=true;
    }
}
