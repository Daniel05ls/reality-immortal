package vn.dicestion.reality;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public final class RealityCombat {
    public static final ResourceKey<DamageType> REALITY_CUT = ResourceKey.create(Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(RealityImmortal.ID, "reality_cut"));
    private RealityCombat() {}
    public static Entity parent(Entity target) {
        if (target instanceof EnderDragonPart part) return part.parentMob;
        if (target instanceof PartEntity<?> part) return part.getParent();
        return target;
    }
    public static boolean canCut(ServerPlayer attacker, Entity original) {
        Entity target = parent(original);
        if (target == attacker || target.isRemoved() || target.isSpectator()) return false;
        if (target instanceof LivingEntity living && !living.isAlive()) return false;
        if (target instanceof Player p) {
            return !p.isCreative() && attacker.server.isPvpAllowed() && attacker.canHarmPlayer(p);
        }
        return target instanceof LivingEntity || (target.isAttackable() && target.isPickable());
    }
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)
                || !p.getMainHandItem().is(RealityImmortal.SWORD.get())) return;
        event.setCanceled(true);
        if (canCut(p, event.getTarget())) {
            cut(p, event.getTarget());
            p.resetAttackStrengthTicker();
        }
    }
    public static void cut(ServerPlayer attacker, Entity original) {
        if (!canCut(attacker, original)) return;
        Entity target = parent(original);
        ServerLevel level = attacker.serverLevel();
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(REALITY_CUT), attacker);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.8,
            target.getZ(), 14, 0.3, 0.6, 0.3, 0.025);
        if (target instanceof LivingEntity living) {
            living.invulnerableTime = 0;
            living.setAbsorptionAmount(0);
            living.setLastHurtByPlayer(attacker);
            living.hurt(source, Float.MAX_VALUE);
            // Execution bypasses ordinary health caps without using Infinity/NaN.
            if (living.isAlive()) {
                living.setHealth(0.0F);
                living.die(source);
            }
            // Preserve the dragon portal/XP sequence and player respawn lifecycle.
            if (!(living instanceof Player) && !(living instanceof EnderDragon) && living.isAlive()) {
                living.discard();
            }
        } else {
            target.hurt(source, Float.MAX_VALUE);
            if (!target.isRemoved()) target.discard();
        }
    }
}