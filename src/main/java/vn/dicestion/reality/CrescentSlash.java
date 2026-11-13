package vn.dicestion.reality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
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

/**
 * A flying crescent blade launched by every swing of the Reality Sword.
 * It travels straight ahead, passes through blocks, cuts everything it
 * touches, and leaves a particle scar along its path. Server-side only.
 */
public final class CrescentSlash {
    private static final double SPEED = 1.5;      // blocks per tick
    private static final double RANGE = 30;       // blocks
    private static final int COOLDOWN_TICKS = 2;  // 0.1s
    private static final int MAX_ACTIVE = 32;
    private static final Map<ServerLevel, List<Slash>> ACTIVE = new WeakHashMap<>();
    private static final Map<UUID, Long> LAST_SLASH = new HashMap<>();
    private record Slash(ServerPlayer player, Vec3 start, Vec3 pos, Vec3 dir) {}
    private CrescentSlash() {}

    public static void tryLaunch(ServerLevel level, ServerPlayer player) {
        if (player.isSpectator()) return;
        long now = level.getGameTime();
        Long last = LAST_SLASH.get(player.getUUID());
        if (last != null && now - last < COOLDOWN_TICKS) return;
        LAST_SLASH.put(player.getUUID(), now);
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
            // The blade itself, facing along its travel direction.
            RealityCombat.crescentSlash(level, pos, s.dir());
            // A faint scar along the path it has crossed.
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);
            // Cut everything the blade passes through. The check radius matches
            // the crescent's size and is wider than one tick of travel, so
            // nothing can slip between two positions.
            for (Entity candidate : level.getEntities(s.player(), new AABB(pos, pos).inflate(2.0),
                    e -> RealityCombat.canCut(s.player(), e))) {
                if (candidate.getBoundingBox().inflate(1.8).contains(pos)) {
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
        LAST_SLASH.clear();
    }
}
