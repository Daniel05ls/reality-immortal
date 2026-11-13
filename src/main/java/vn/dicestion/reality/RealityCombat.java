package vn.dicestion.reality;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    // Right-click charge thresholds for the sword-qi blade: a hold shorter
    // than MIN_CHARGE_TICKS counts as a tap (Space Rift); the charge is
    // complete at FULL_CHARGE_TICKS.
    public static final int MIN_CHARGE_TICKS = 20;  // 1s
    public static final int FULL_CHARGE_TICKS = 40; // 2s
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
            attacker.getLookAngle(), 1.0);
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

    /**
     * The sword-qi charging effect while the player holds right-click:
     * golden particles spiral inward toward the sword as the charge
     * builds, with a bright flash and a chime when the charge completes.
     */
    public static void chargeEffect(ServerLevel level, ServerPlayer player, int heldTicks) {
        Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(0.8));
        double angle = heldTicks * 0.5;
        double radius = Math.max(0.5, 2.0 - heldTicks * 0.04);
        for (int i = 0; i < 3; i++) {
            double a = angle + i * (Math.PI * 2 / 3);
            Vec3 p = center.add(Math.cos(a) * radius, Math.sin(heldTicks * 0.3 + i) * 0.4, Math.sin(a) * radius);
            level.sendParticles(GOLD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        if (heldTicks % 5 == 0) {
            level.sendParticles(WHITE, center.x, center.y, center.z, 2, 0.2, 0.2, 0.2, 0);
        }
        if (heldTicks == FULL_CHARGE_TICKS) {
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 12, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 0.6F, 1.2F);
        }
    }

    private static final DustParticleOptions CYAN_EDGE = new DustParticleOptions(new Vector3f(1F,0.73F,0.08F),0.7F);
    private static final DustParticleOptions ICE_CORE = new DustParticleOptions(new Vector3f(1F,0.98F,0.82F),0.46F);
    private static final DustParticleOptions BLUE_TRAIL = new DustParticleOptions(new Vector3f(0.85F,0.38F,0.025F),0.9F);

    public static void swordQi(ServerLevel level,Vec3 center,Vec3 look,double scale) {
        SwordArc.emit(level,center,look,scale,0F,9);
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
    public static void shatterBlocks(ServerLevel level, Vec3 from, Vec3 to, Vec3 dir, double scale) {
        Basis basis = bladeBasis(dir);
        AABB box = new AABB(from, to).inflate(3.0 * scale);
        Vec3 travel = to.subtract(from);
        double travelLenSq = travel.lengthSqr();
        for (BlockPos p : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            if (level.isOutsideBuildHeight(p)) continue;

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
            double s1 = off.dot(basis.u()) * 0.8660254 - off.dot(basis.v()) * 0.5;
            double p1 = off.dot(basis.u()) * 0.5 + off.dot(basis.v()) * 0.8660254;
            double t1=s1/(3.0*scale);
            boolean onStroke1 = Math.abs(t1)<=1 && Math.abs(p1-1.3*scale*(1-t1*t1))<=0.7*scale;
            double t2=s1/(2.34*scale);
            boolean onStroke2 = Math.abs(t2)<=1 && Math.abs(p1+0.8*scale-0.9*scale*(1-t2*t2))<=0.55*scale;
            if (!onStroke1 && !onStroke2) continue;
            // Read world state only after the geometry test; never load distant chunks.
            if (!level.hasChunkAt(p)) continue;
            BlockState state=level.getBlockState(p);
            if (!state.isAir() && !state.liquid()) BlockCutQueue.enqueue(level,p,state);
        }
    }
}
