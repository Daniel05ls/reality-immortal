package vn.dicestion.reality;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
public final class AngelFlight {
    private static final String GRANTED = "realityimmortal:angel_flight";
    private static final String PREVIOUS = "realityimmortal:previous_mayfly";
    private static final String ELYTRA = "realityimmortal:elytra_mode";
    private AngelFlight() {}
    public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("canh")
            .executes(c -> switchMode(c.getSource().getPlayerOrException(),
                !c.getSource().getPlayerOrException().getPersistentData().getBoolean(ELYTRA)))
            .then(net.minecraft.commands.Commands.literal("elytra")
                .executes(c -> switchMode(c.getSource().getPlayerOrException(),true)))
            .then(net.minecraft.commands.Commands.literal("angel")
                .executes(c -> switchMode(c.getSource().getPlayerOrException(),false))));
    }
    private static int switchMode(ServerPlayer p, boolean elytra) {
        if (!p.getItemBySlot(EquipmentSlot.CHEST).is(RealityImmortal.CHEST.get())) {
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Hãy mặc áo giáp có cánh trước."),true);
            return 0;
        }
        p.getPersistentData().putBoolean(ELYTRA,elytra);
        boolean airborne = !p.onGround() && !p.isInWater();
        p.stopFallFlying();
        tick(p);
        if (!p.isCreative() && !p.isSpectator() && airborne) {
            if (elytra) p.tryToStartFallFlying();
            else { p.getAbilities().flying=true; p.onUpdateAbilities(); }
        }
        p.displayClientMessage(net.minecraft.network.chat.Component.literal(
            elytra ? "Cánh: Elytra — lượn và dùng pháo hoa" : "Cánh: Thiên thần — Space ×2 bật bay, Space lên, Shift xuống"),true);
        return 1;
    }
    public static void tick(ServerPlayer p) {
        var data = p.getPersistentData();
        var abilities = p.getAbilities();
        if (p.isCreative() || p.isSpectator()) {
            data.remove(GRANTED); data.remove(PREVIOUS); return;
        }
        boolean equipped = p.isAlive() && p.getItemBySlot(EquipmentSlot.CHEST).is(RealityImmortal.CHEST.get());
        boolean wings = equipped && !data.getBoolean(ELYTRA);
        if (wings) {
            if (!data.getBoolean(GRANTED)) {
                data.putBoolean(PREVIOUS, abilities.mayfly); data.putBoolean(GRANTED,true);
            }
            if (!abilities.mayfly) { abilities.mayfly=true; p.onUpdateAbilities(); }
            if (abilities.flying) p.fallDistance=0;
        } else if (data.getBoolean(GRANTED)) {
            boolean previous=data.getBoolean(PREVIOUS);
            data.remove(GRANTED); data.remove(PREVIOUS);
            if (!previous) {
                boolean wasFlying=abilities.flying;
                abilities.mayfly=false; abilities.flying=false; p.onUpdateAbilities();
                if (wasFlying && p.isAlive() && !equipped)
                    p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,100,0,true,false));
            }
        }
    }
}
