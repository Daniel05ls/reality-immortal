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
import net.minecraft.world.effect.MobEffects;
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
        // Only a fully charged critical hit releases the flying sword-qi
        // blade; ordinary swings cut the target but launch nothing.
        if (isChargedCrit(p, event.getTarget())) {
            CrescentSlash.tryLaunch(p.serverLevel(), p);
        }
        if (canCut(p, event.getTarget())) {
            cut(p, event.getTarget());
            p.resetAttackStrengthTicker();
        }
    }

    /**
     * True when this attack is a fully charged critical hit: the attack
     * meter is completely full (the sword qi has finished charging) and
     * the strike lands while falling, as a vanilla critical hit does.
     */
    public static boolean isChargedCrit(ServerPlayer attacker, Entity original) {
        if (attacker.getAttackStrengthScale(0.0F) < 1.0F) return false;
        Entity target = parent(original);
        return target instanceof LivingEntity
            && attacker.fallDistance > 0.0F
            && !attacker.onGround()
            && !attacker.onClimbable()
            && !attacker.isInWater()
            && !attacker.hasEffect(MobEffects.BLINDNESS)
            && !attacker.isPassenger()
            && !attacker.isSprinting();
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
        // Three crescents nested into a slightly curved X: two large
        // crescents cross as the strokes of the X, curving toward each
        // other, and a smaller crescent sits nested at the crossing,
        // curving the other way. Bright white core, golden glowing edges.
        Basis basis = bladeBasis(look);
        Vec3 u = basis.u();
        Vec3 v = basis.v();
        Vec3 d1 = u.add(v).normalize();      // bottom-left -> top-right
        Vec3 d2 = u.subtract(v).normalize(); // top-left -> bottom-right
        crescent(level, center, d1, d2.scale(-1), 6.0, 0.8);
        crescent(level, center, d2, d1, 6.0, 0.8);
        crescent(level, center, d1, d2, 2.6, 0.4);
    }

    private static void crescent(ServerLevel level, Vec3 center, Vec3 dir, Vec3 bulgeDir, double length, double bulge) {
        // One crescent stroke: a curved arc, thick in the middle and pointed
        // at both ends, with a bright white core and golden edges.
        for (int i = 0; i <= 14; i++) {
            double t = i / 7.0 - 1.0; // -1 .. 1 along the stroke
            Vec3 p = center.add(dir.scale(t * length / 2)).add(bulgeDir.scale(bulge * (1 - t * t)));
            level.sendParticles(WHITE, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            double half = 0.28 * (1 - t * t);
            if (half > 0.05) {
                Vec3 edgeUp = p.add(bulgeDir.scale(half));
                Vec3 edgeDown = p.add(bulgeDir.scale(-half));
                level.sendParticles(GOLD, edgeUp.x, edgeUp.y, edgeUp.z, 1, 0, 0, 0, 0);
                level.sendParticles(GOLD, edgeDown.x, edgeDown.y, edgeDown.z, 1, 0, 0, 0, 0);
            }
            if (Math.abs(t) <= 0.3) {
                Vec3 coreUp = p.add(bulgeDir.scale(0.14));
                Vec3 coreDown = p.add(bulgeDir.scale(-0.14));
                level.sendParticles(LIGHT_GOLD, coreUp.x, coreUp.y, coreUp.z, 1, 0, 0, 0, 0);
                level.sendParticles(LIGHT_GOLD, coreDown.x, coreDown.y, coreDown.z, 1, 0, 0, 0, 0);
            }
        }
    }

    /** The in-plane axes of the sword-qi blade for a given facing direction. */
    private record Basis(Vec3 u, Vec3 v) {}

    private static Basis bladeBasis(Vec3 look) {
        Vec3 forward = look.normalize();
        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        return new Basis(right, up);
    }

    /**
     * Smashes every block the flying sword-qi blade passes through on its
     * way from {@code from} to {@code to}. Only blocks the X-shaped blade
     * actually crosses are broken: the two diagonal strokes, allowing for
     * their curve. Blocks break with their normal drops, so structures
     * are torn apart and their blocks fall as items. Even unbreakable
     * blocks (bedrock and the like) are torn out of the world, dropping
     * their block item when they have one.
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
            // Keep only blocks inside one of the two diagonal strokes of
            // the X. The strokes run along (u+v) and (u-v), so the
            // coordinates along and across one stroke are the across and
            // along coordinates of the other.
            Vec3 c = Vec3.atCenterOf(p);
            Vec3 rel = c.subtract(from);
            double along = travelLenSq < 1e-6 ? 0 : rel.dot(travel) / travelLenSq;
            if (along < 0 || along > 1) continue;
            Vec3 closest = from.add(travel.scale(along));
            Vec3 off = c.subtract(closest);
            double s1 = (off.dot(basis.u()) + off.dot(basis.v())) / Math.sqrt(2);
            double p1 = (off.dot(basis.u()) - off.dot(basis.v())) / Math.sqrt(2);
            boolean onStroke1 = Math.abs(s1) <= 3.0 && Math.abs(p1) <= 1.0;
            boolean onStroke2 = Math.abs(p1) <= 3.0 && Math.abs(s1) <= 1.0;
            if (!onStroke1 && !onStroke2) continue;
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