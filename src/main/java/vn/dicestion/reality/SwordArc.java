package vn.dicestion.reality;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public record SwordArc(double x,double y,double z,double dx,double dy,double dz,float scale,float speed,int life)
        implements CustomPacketPayload {
    public static final Type<SwordArc> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(RealityImmortal.ID,"sword_arc"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SwordArc> CODEC=new StreamCodec<>() {
        public SwordArc decode(RegistryFriendlyByteBuf b) {
            return new SwordArc(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),
                b.readFloat(),b.readFloat(),b.readVarInt());
        }
        public void encode(RegistryFriendlyByteBuf b,SwordArc a) {
            b.writeDouble(a.x);b.writeDouble(a.y);b.writeDouble(a.z);b.writeDouble(a.dx);b.writeDouble(a.dy);b.writeDouble(a.dz);
            b.writeFloat(a.scale);b.writeFloat(a.speed);b.writeVarInt(a.life);
        }
    };
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE,CODEC,(arc,ctx)->
            ctx.enqueueWork(()->vn.dicestion.reality.client.SwordArcRenderer.add(arc)));
    }
    public static void emit(ServerLevel level,Vec3 center,Vec3 direction,double scale,float speed,int life) {
        PacketDistributor.sendToPlayersNear(level,null,center.x,center.y,center.z,96,
            new SwordArc(center.x,center.y,center.z,direction.x,direction.y,direction.z,(float)scale,speed,life));
    }
}
