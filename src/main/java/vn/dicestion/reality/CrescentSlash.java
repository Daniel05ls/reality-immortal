package vn.dicestion.reality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Vector3f;

/**
 * A flying sword-qi blade released by holding right-click to charge and
 * letting go (see RealitySword). The charged blade is wide: its X spans
 * about 7 blocks. It travels straight ahead, smashes through blocks
 * (dropping them as items), cuts everything it touches, and leaves a
 * golden glowing wake behind it. Server-side only.
 */
public final class CrescentSlash {
    private static final double SPEED = 2.0;      // blocks per tick
    private static final double RANGE = 50;       // blocks
    private static final double SCALE = 5.0 / 3.0; // wide charged blade (10-block strokes)
    private static final int MAX_ACTIVE = 32;
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1F, 0.79F, 0.24F), 1.0F);
    private static final Map<ServerLevel, List<Slash>> ACTIVE = new WeakHashMap<>();
    private record Slash(ServerPlayer player, Vec3 start, Vec3 pos, Vec3 dir) {}
    private CrescentSlash() {}

    public static void tryLaunch(ServerLevel level, ServerPlayer player) {
        if (player.isSpectator() || !player.isAlive()) return;
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(dir.scale(1.5));
        List<Slash> slashes = ACTIVE.computeIfAbsent(level, k -> new ArrayList<>());
        if (slashes.size() >= MAX_ACTIVE) slashes.remove(0);
        slashes.add(new Slash(player, start, start, dir));
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.PLAYERS, 0.4F, 1.2F);
    }

    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<Slash> slashes = ACTIVE.get(level);
        if (slashes == null) return;
        List<Slash> next = new ArrayList<>(slashes.size());
        for (Slash s : slashes) {
            if (s.player().isRemoved()) continue;
            Vec3 pos = s.pos().add(s.dir().scale(SPEED));
            // Smash every block the blade passes through, dropping the blocks.
            RealityCombat.shatterBlocks(level, s.pos(), pos, s.dir(), SCALE);
            // The blade itself, facing along its travel direction.
            RealityCombat.swordQi(level, pos, s.dir(), SCALE);
            // A golden glowing wake behind the blade.
            Vec3 behind = pos.add(s.dir().scale(-0.8));
            level.sendParticles(GOLD, behind.x, behind.y, behind.z, 2, 0.15, 0.15, 0.15, 0);
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0.01);
            // Cut everything the blade passes through. The check radius matches
            // the X's reach (its stroke tips are 3 blocks from the center,
            // scaled up for the wide blade) and is wider than one tick of
            // travel, so nothing can slip between two positions.
            for (Entity candidate : level.getEntities(s.player(), new AABB(pos, pos).inflate(3.4 * SCALE),
                    e -> RealityCombat.canCut(s.player(), e))) {
                if (candidate.getBoundingBox().inflate(3.0 * SCALE).contains(pos)) {
                    RealityCombat.cut(s.player(), candidate);
                }
            }
            if (pos.distanceTo(s.start()) < RANGE) next.add(new Slash(s.player(), s.start(), pos, s.dir()));
        }
        if (next.isEmpty()) ACTIVE.remove(level);
        else ACTIVE.put(level, next);
    }

    public static void stop(ServerStoppedEvent event) {
        ACTIVE.clear();
    }
}
