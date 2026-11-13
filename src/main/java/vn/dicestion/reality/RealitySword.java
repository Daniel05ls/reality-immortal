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
        // Every swing launches a flying crescent blade (server side only).
        // Returning false keeps the normal swing animation.
        if (entity.level() instanceof ServerLevel level && entity instanceof ServerPlayer sp) {
            CrescentSlash.tryLaunch(level, sp);
        }
        return false;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            SpaceRift.cast(server, sp);
            player.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
