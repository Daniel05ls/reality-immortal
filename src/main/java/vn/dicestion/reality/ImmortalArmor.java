package vn.dicestion.reality;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class ImmortalArmor extends ArmorItem {
    public ImmortalArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    @Override
    public boolean isEnchantable(ItemStack stack) { return !stack.isEnchanted(); }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.realityimmortal.armor").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.add(Component.translatable("tooltip.realityimmortal.enchantable").withStyle(ChatFormatting.AQUA));
    }
}
