package vn.dicestion.reality;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
public final class AngelFlight {
    private static final String GRANTED = "realityimmortal:angel_flight";
    private static final String PREVIOUS = "realityimmortal:previous_mayfly";
    private AngelFlight() {}
    public static void tick(ServerPlayer p) {
        var data = p.getPersistentData();
        var abilities = p.getAbilities();
        if (p.isCreative() || p.isSpectator()) {
            data.remove(GRANTED); data.remove(PREVIOUS); return;
        }
        boolean wings = p.isAlive() && p.getItemBySlot(EquipmentSlot.CHEST).is(RealityImmortal.CHEST.get());
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
                if (wasFlying && p.isAlive())
                    p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,100,0,true,false));
            }
        }
    }
}
