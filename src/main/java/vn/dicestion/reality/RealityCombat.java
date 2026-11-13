package vn.dicestion.reality;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.joml.Vector3f;

public final class RealityCombat {
    public static final ResourceKey<DamageType> REALITY_CUT = ResourceKey.create(Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(RealityImmortal.ID, "reality_cut"));
    private static final DustParticleOptions CYAN = new DustParticleOptions(new Vector3f(0.15F, 0.95F, 1F), 1.2F);
    private static final DustParticleOptions PURPLE = new DustParticleOptions(new Vector3f(0.65F, 0.15F, 1F), 1.2F);
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
        // No other exception: bosses, modded entities and players (including
        // creative mode and servers with PvP disabled) are all cut.
        return true;
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
        crescentSlash(level, new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.55, target.getZ()),
            attacker.getLookAngle());
        // Impact sparks where the cut lands.
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + target.getBbHeight() * 0.55,
            target.getZ(), 8, 0.15, 0.15, 0.15, 0.05);
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
            // Anything that still refuses to die (a boss phase, a death-cancel
            // from another mod, a clamped health value) is removed from the
            // world outright. The dragon's portal/XP sequence is preserved
            // because die() already ran; this only fires if something kept
            // it alive afterwards. Players keep the normal respawn lifecycle.
            if (!(living instanceof Player) && living.isAlive()) {
                living.discard();
            }
        } else {
            target.hurt(source, Float.MAX_VALUE);
            if (!target.isRemoved()) target.discard();
        }
    }

    public static void crescentSlash(ServerLevel level, Vec3 center, Vec3 look) {
        // A single crescent moon in the plane facing along the given direction,
        // tilted diagonally like a sword cut. The outer arc is the bright cyan
        // edge; the inner arc is smaller with its center pushed away from the
        // belly, so the two arcs meet at pointed tips and the shape is
        // thickest in the middle, like a crescent moon.
        Vec3 forward = look.normalize();
        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        double tilt = Math.toRadians(35);
        Vec3 u = right.scale(Math.cos(tilt)).add(up.scale(Math.sin(tilt)));
        Vec3 v = right.scale(-Math.sin(tilt)).add(up.scale(Math.cos(tilt)));
        arc(level, center, u, v, 1.7, -110, 110, 5, CYAN);
        arc(level, center.add(u.scale(-0.5)), u, v, 1.6, -93, 93, 5, PURPLE);
    }

    private static void arc(ServerLevel level, Vec3 center, Vec3 u, Vec3 v, double radius,
            double startDegrees, double endDegrees, double stepDegrees, DustParticleOptions options) {
        for (double a = startDegrees; a <= endDegrees; a += stepDegrees) {
            double r = Math.toRadians(a);
            Vec3 p = center.add(u.scale(Math.cos(r) * radius)).add(v.scale(Math.sin(r) * radius));
            level.sendParticles(options, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }
}