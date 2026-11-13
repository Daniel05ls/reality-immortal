package vn.dicestion.reality;

import java.util.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.joml.Vector3f;

/** Short-lived server particles. Does not change blocks or load distant chunks. */
public final class SpaceRift {
    private static final Map<ServerLevel, List<Rift>> ACTIVE = new WeakHashMap<>();
    private static final DustParticleOptions CYAN = new DustParticleOptions(new Vector3f(0.15F, 0.95F, 1F), 1.3F);
    private static final DustParticleOptions PURPLE = new DustParticleOptions(new Vector3f(0.65F, 0.15F, 1F), 1.5F);
    private record Rift(Vec3 start, Vec3 end, Vec3 side, long created) {}
    private SpaceRift() {}
    public static void cast(ServerLevel level, ServerPlayer player) {
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(direction.scale(1.5));
        Vec3 end = start.add(direction.scale(32));
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 0.001) side = new Vec3(1, 0, 0);
        side = side.normalize();
        List<Rift> rifts = ACTIVE.computeIfAbsent(level, k -> new ArrayList<>());
        if (rifts.size() >= 24) rifts.remove(0);
        rifts.add(new Rift(start, end, side, level.getGameTime()));
        Set<UUID> struck = new HashSet<>();
        for (Entity candidate : level.getEntities(player, new AABB(start, end).inflate(3),
                e -> RealityCombat.canCut(player, e))) {
            AABB box = candidate.getBoundingBox().inflate(1.25);
            if ((box.contains(start) || box.clip(start, end).isPresent())
                    && struck.add(RealityCombat.parent(candidate).getUUID())) {
                RealityCombat.cut(player, candidate);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
            SoundSource.PLAYERS, 0.8F, 1.4F);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS, 0.8F, 0.6F);
    }
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<Rift> rifts = ACTIVE.get(level);
        if (rifts == null) return;
        long now = level.getGameTime();
        rifts.removeIf(r -> now - r.created >= 24);
        if (rifts.isEmpty()) { ACTIVE.remove(level); return; }
        if ((now & 1) != 0) return;
        for (Rift r : rifts) {
            float age = (now - r.created) / 24.0F;
            for (int i = 0; i <= 32; i++) {
                double t = i / 32.0;
                Vec3 center = r.start.lerp(r.end, t);
                double width = Math.sin(t * Math.PI) * (0.15 + 0.8 * Math.sin(age * Math.PI));
                double jagged = (i % 2 == 0 ? 0.07 : -0.07);
                Vec3 a = center.add(r.side.scale(width + jagged));
                Vec3 b = center.subtract(r.side.scale(width + jagged));
                level.sendParticles(CYAN, a.x, a.y, a.z, 1, 0, 0, 0, 0);
                level.sendParticles(PURPLE, b.x, b.y, b.z, 1, 0, 0, 0, 0);
                if (i % 4 == 0) level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    center.x, center.y, center.z, 2, 0.05, 0.15, 0.05, 0.01);
            }
        }
    }
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); }
}
