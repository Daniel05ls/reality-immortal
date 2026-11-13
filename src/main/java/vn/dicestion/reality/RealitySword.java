package vn.dicestion.reality;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public final class RealitySword extends SwordItem {
    public RealitySword() {
        // Player base attack 1 + netherite tier bonus 4 + 995 = 1000.
        super(Tiers.NETHERITE, RealityImmortal.properties()
            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 995, -2.4F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (level instanceof ServerLevel server) {
            // Hostile entities only: no players, pets or passive animals.
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(8), e -> e instanceof Enemy
                        && e.isAlive() && e.distanceToSqr(player) <= 64
                        && !player.isAlliedTo(e) && player.hasLineOfSight(e))) {
                target.hurt(player.damageSources().playerAttack(player), 1000.0F);
                server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1,
                    target.getZ(), 16, 0.3, 0.6, 0.3, 0.05);
            }
            server.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1,
                player.getZ(), 120, 2, 1, 2, 0.2);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 0.65F);
            player.getCooldowns().addCooldown(this, 100);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
