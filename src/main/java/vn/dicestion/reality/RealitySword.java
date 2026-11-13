package vn.dicestion.reality;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class RealitySword extends SwordItem {
    public RealitySword() {
        // The largest finite float: effectively infinite damage. The value
        // never reaches a damage formula, because RealityCombat cancels the
        // vanilla attack before it is computed; the execution guarantees
        // the kill.
        super(Tiers.NETHERITE, RealityImmortal.properties()
            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, Float.MAX_VALUE, -2.0F)));
    }
    @Override
    public boolean isEnchantable(ItemStack stack) { return !stack.isEnchanted(); }
    @Override
    public int getEnchantmentValue() { return 30; }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.realityimmortal.infinity").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.add(Component.translatable("tooltip.realityimmortal.rift").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.realityimmortal.enchantable").withStyle(ChatFormatting.GRAY));
    }
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        // Every normal swing draws the X-shaped slash in front of the
        // player (server side only). Returning false keeps the normal
        // swing animation.
        if (entity.level() instanceof ServerLevel level && entity instanceof ServerPlayer sp) {
            RealityCombat.swordQi(level, sp.getEyePosition().add(sp.getLookAngle().scale(1.5)),
                sp.getLookAngle(), 1.0);
        }
        return false;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Hold right-click to charge sword qi; releasing decides the
        // outcome (see releaseUsing).
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // effectively unlimited hold
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK; // sword held up while charging
    }
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level instanceof ServerLevel server && entity instanceof ServerPlayer sp) {
            int held = getUseDuration(stack, entity) - remainingUseDuration;
            RealityCombat.chargeEffect(server, sp, held);
        }
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof ServerPlayer sp)) return;
        if (timeCharged < RealityCombat.MIN_CHARGE_TICKS) {
            // Quick tap: Space Rift, as before (if its cooldown allows).
            if (!sp.getCooldowns().isOnCooldown(this)) {
                SpaceRift.cast(server, sp);
                sp.getCooldowns().addCooldown(this, 60);
            }
            return;
        }
        // Held long enough: release the wide sword-qi blade.
        CrescentSlash.tryLaunch(server, sp);
    }
}
