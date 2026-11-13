package vn.dicestion.reality;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.joml.Vector3f;

public final class RealityCombat {
    public static final ResourceKey<DamageType> REALITY_CUT = ResourceKey.create(Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(RealityImmortal.ID, "reality_cut"));
    private static final DustParticleOptions WHITE = new DustParticleOptions(new Vector3f(1F, 1F, 1F), 1.3F);
    private static final DustParticleOptions LIGHT_GOLD = new DustParticleOptions(new Vector3f(1F, 0.91F, 0.66F), 1.1F);
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1F, 0.79F, 0.24F), 1.0F);
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
        swordQi(level, new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.55, target.getZ()),
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

    public static void swordQi(ServerLevel level, Vec3 center, Vec3 look) {
        // A wuxia sword-qi blade: a long, thin arc of white-gold energy in
        // the plane facing along the given direction, tilted diagonally like
        // a sword cut. Bright white core, golden glowing edges, thickest in
        // the middle and pointed at both ends, with a leading point at the
        // apex of the arc.
        Basis basis = bladeBasis(look);
        Vec3 u = basis.u();
        Vec3 v = basis.v();
        for (int i = 0; i <= 16; i++) {
            double t = i / 8.0 - 1.0; // -1 .. 1 along the blade
            Vec3 p = center.add(u.scale(t * 2.5)).add(v.scale(0.8 * (1 - t * t)));
            level.sendParticles(WHITE, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            double half = 0.3 * (1 - t * t);
            if (half > 0.05) {
                Vec3 a = p.add(v.scale(half));
                Vec3 b = p.add(v.scale(-half));
                level.sendParticles(GOLD, a.x, a.y, a.z, 1, 0, 0, 0, 0);
                level.sendParticles(GOLD, b.x, b.y, b.z, 1, 0, 0, 0, 0);
            }
            if (Math.abs(t) <= 0.3) {
                Vec3 a = p.add(v.scale(0.15));
                Vec3 b = p.add(v.scale(-0.15));
                level.sendParticles(LIGHT_GOLD, a.x, a.y, a.z, 1, 0, 0, 0, 0);
                level.sendParticles(LIGHT_GOLD, b.x, b.y, b.z, 1, 0, 0, 0, 0);
            }
        }
        // Leading point at the apex of the arc.
        Vec3 apex = center.add(v.scale(0.8));
        Vec3 tip = apex.add(v.scale(0.25));
        level.sendParticles(WHITE, tip.x, tip.y, tip.z, 1, 0, 0, 0, 0);
        tip = apex.add(v.scale(0.45));
        level.sendParticles(WHITE, tip.x, tip.y, tip.z, 1, 0, 0, 0, 0);
    }

    /** The in-plane axes of the sword-qi blade for a given facing direction. */
    private record Basis(Vec3 u, Vec3 v) {}

    private static Basis bladeBasis(Vec3 look) {
        Vec3 forward = look.normalize();
        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        double tilt = Math.toRadians(35);
        return new Basis(
            right.scale(Math.cos(tilt)).add(up.scale(Math.sin(tilt))),
            right.scale(-Math.sin(tilt)).add(up.scale(Math.cos(tilt))));
    }

    /**
     * Smashes every block the flying sword-qi blade passes through on its
     * way from {@code from} to {@code to}. Blocks break with their normal
     * drops, so structures are torn apart and their blocks fall as items.
     * Even unbreakable blocks (bedrock and the like) are torn out of the
     * world, dropping their block item when they have one.
     */
    public static void shatterBlocks(ServerLevel level, Vec3 from, Vec3 to, Vec3 dir) {
        Basis basis = bladeBasis(dir);
        AABB box = new AABB(from, to).inflate(3.0);
        Vec3 travel = to.subtract(from);
        double travelLenSq = travel.lengthSqr();
        for (BlockPos p : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            if (level.isOutsideBuildHeight(p)) continue;
            BlockState state = level.getBlockState(p);
            if (state.isAir() || state.liquid()) continue;
            // Keep only blocks inside the thin slab the blade sweeps through:
            // within the blade's width along u and its thickness along v.
            Vec3 c = Vec3.atCenterOf(p);
            Vec3 rel = c.subtract(from);
            double along = travelLenSq < 1e-6 ? 0 : rel.dot(travel) / travelLenSq;
            if (along < 0 || along > 1) continue;
            Vec3 closest = from.add(travel.scale(along));
            Vec3 off = c.subtract(closest);
            if (Math.abs(off.dot(basis.u())) > 2.6 || Math.abs(off.dot(basis.v())) > 0.9) continue;
            if (state.getDestroySpeed(level, p) < 0) {
                Item item = state.getBlock().asItem();
                level.levelEvent(2001, p, Block.getId(state));
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                if (item != Items.AIR) {
                    Block.popResource(level, p, new ItemStack(item));
                }
            } else {
                level.destroyBlock(p, true);
            }
        }
    }
}