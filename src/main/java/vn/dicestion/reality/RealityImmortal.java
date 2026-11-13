package vn.dicestion.reality;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RealityImmortal.ID)
public final class RealityImmortal {
    public static final String ID = "realityimmortal";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, ID);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> IMMORTAL = MATERIALS.register("immortal", () ->
        new ArmorMaterial(Map.of(ArmorItem.Type.HELMET, 5, ArmorItem.Type.CHESTPLATE, 10,
            ArmorItem.Type.LEGGINGS, 8, ArmorItem.Type.BOOTS, 5, ArmorItem.Type.BODY, 10),
            30, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(Items.NETHER_STAR),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ID, "immortal"))),
            5.0F, 0.25F));
    public static final DeferredItem<RealitySword> SWORD = ITEMS.register("reality_sword", RealitySword::new);
    public static final DeferredItem<ArmorItem> HELMET = armor("immortal_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> CHEST = armor("immortal_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> LEGS = armor("immortal_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> BOOTS = armor("immortal_boots", ArmorItem.Type.BOOTS);
    // +60 max health: three extra health bars (30 hearts) while the full
    // Immortal set is worn.
    private static final ResourceLocation EXTRA_HEALTH_ID = ResourceLocation.fromNamespaceAndPath(ID, "immortal_health");

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GOD_TAB = TABS.register("god_arsenal", () ->
        CreativeModeTab.builder().title(Component.literal("God Arsenal"))
            .icon(() -> SWORD.get().getDefaultInstance())
            .displayItems((params, out) -> {
                out.accept(SWORD.get()); out.accept(HELMET.get()); out.accept(CHEST.get());
                out.accept(LEGS.get()); out.accept(BOOTS.get());
            }).build());
    public RealityImmortal(IEventBus bus) {
        MATERIALS.register(bus);
        ITEMS.register(bus);
        NeoForge.EVENT_BUS.addListener(AngelFlight::swap);
        TABS.register(bus);
        NeoForge.EVENT_BUS.addListener(RealityImmortal::protect);
        NeoForge.EVENT_BUS.addListener(RealityImmortal::denyDeath);
        NeoForge.EVENT_BUS.addListener(RealityImmortal::onCommand);
        NeoForge.EVENT_BUS.addListener(RealityImmortal::tick);
        NeoForge.EVENT_BUS.addListener(RealityCombat::onAttack);
        NeoForge.EVENT_BUS.addListener(SpaceRift::tick);
        NeoForge.EVENT_BUS.addListener(SpaceRift::stop);
        NeoForge.EVENT_BUS.addListener(CrescentSlash::tick);
        NeoForge.EVENT_BUS.addListener(CrescentSlash::stop);
    }

    public static Item.Properties properties() {
        return new Item.Properties().fireResistant().rarity(Rarity.EPIC)
            .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private static DeferredItem<ArmorItem> armor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ImmortalArmor(IMMORTAL, type,
            properties().durability(type.getDurability(37))));
    }


    public static boolean fullSet(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(HELMET.get())
            && p.getItemBySlot(EquipmentSlot.CHEST).is(CHEST.get())
            && p.getItemBySlot(EquipmentSlot.LEGS).is(LEGS.get())
            && p.getItemBySlot(EquipmentSlot.FEET).is(BOOTS.get());
    }

    private static void protect(LivingIncomingDamageEvent event) {
        // Full set blocks every damage source, including generic_kill from commands
        // such as /damage. Only the Reality Sword's own cut may pass, so the sword
        // still beats the armor in PvP.
        if (event.getEntity() instanceof ServerPlayer p && fullSet(p)
                && !event.getSource().is(RealityCombat.REALITY_CUT)) {
            event.setCanceled(true);
        }
    }

    private static void denyDeath(LivingDeathEvent event) {
        // /kill bypasses damage events entirely: LivingEntity.kill() sets health to 0
        // and calls die() directly, so the death itself must be cancelled here.
        // Health is restored at once, otherwise the player would stay at 0 HP and
        // count as "not alive" even though death was cancelled.
        // The Reality Sword's cut is still allowed through, preserving PvP behavior.
        if (event.getEntity() instanceof ServerPlayer p && fullSet(p)
                && !event.getSource().is(RealityCombat.REALITY_CUT)) {
            event.setCanceled(true);
            p.setHealth(p.getMaxHealth());
            p.clearFire();
            p.setAirSupply(p.getMaxAirSupply());
        }
    }

    private static void onCommand(CommandEvent event) {
        // /clear and /item would otherwise strip Reality gear straight off the
        // player, so a command that would remove a protected item is cancelled.
        // Commands that do not touch Reality gear are left alone.
        // ParseResults holds a CommandContextBuilder, not a CommandContext: the
        // input comes from its reader, and the context must be built from it
        // the same way the dispatcher does.
        var parse = event.getParseResults();
        String input = parse.getReader().getString().stripLeading();
        if (input.startsWith("/")) input = input.substring(1);
        String name = input.split("\\s+", 2)[0];
        int colon = name.indexOf(':');
        if (colon >= 0) name = name.substring(colon + 1);
        if (!name.equals("clear") && !name.equals("item")) return;

        CommandContext<CommandSourceStack> context = parse.getContext().build(parse.getReader().getString());
        CommandSourceStack source = context.getSource();
        if (name.equals("clear")) {
            if (clearTargets(context, source).stream().anyMatch(p -> wouldClearGear(p, context))) {
                deny(event, source);
            }
        } else if (itemTargets(context).stream().anyMatch(e -> wouldReplaceGear(e, context))) {
            deny(event, source);
        }
    }

    private static List<ServerPlayer> clearTargets(CommandContext<CommandSourceStack> context, CommandSourceStack source) {
        try {
            return EntityArgument.getPlayers(context, "targets").stream().toList();
        } catch (CommandSyntaxException e) {
            // The selector matched nobody, so the command fails on its own.
            return List.of();
        } catch (IllegalArgumentException e) {
            // Plain /clear without a target argument affects its source.
            return source.getEntity() instanceof ServerPlayer p ? List.of(p) : List.of();
        }
    }

    private static List<Entity> itemTargets(CommandContext<CommandSourceStack> context) {
        try {
            // getEntities returns Collection<? extends Entity>, which cannot be
            // returned as List<Entity> directly, so each element is cast first.
            return EntityArgument.getEntities(context, "targets").stream().map(Entity.class::cast).toList();
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            // Block targets have no "targets" argument; a selector matching
            // nothing makes the command fail on its own either way.
            return List.of();
        }
    }

    private static boolean wouldClearGear(ServerPlayer p, CommandContext<CommandSourceStack> context) {
        // /clear with maxCount 0 only counts items, it removes nothing.
        try {
            if (context.getArgument("maxCount", Integer.class) == 0) return false;
        } catch (IllegalArgumentException ignored) {
        }
        // getItemPredicate returns a Result, which is a Predicate<ItemStack>,
        // not an ItemPredicate.
        Predicate<ItemStack> filter = null;
        try {
            filter = ItemPredicateArgument.getItemPredicate(context, "item");
        } catch (IllegalArgumentException ignored) {
            // No item filter: /clear removes everything the player carries.
        }
        for (ItemStack stack : p.getInventory().items) {
            if (matchesGear(stack, filter)) return true;
        }
        for (ItemStack stack : p.getInventory().armor) {
            if (matchesGear(stack, filter)) return true;
        }
        for (ItemStack stack : p.getInventory().offhand) {
            if (matchesGear(stack, filter)) return true;
        }
        return false;
    }

    private static boolean matchesGear(ItemStack stack, Predicate<ItemStack> filter) {
        return !stack.isEmpty() && isRealityGear(stack) && (filter == null || filter.test(stack));
    }

    private static boolean wouldReplaceGear(Entity entity, CommandContext<CommandSourceStack> context) {
        // Covers replace, modify and copy: all of them take the destination slot
        // as "slot", and all of them remove what is currently in that slot.
        int slot;
        try {
            slot = SlotArgument.getSlot(context, "slot");
        } catch (IllegalArgumentException e) {
            return false;
        }
        return isRealityGear(entity.getSlot(slot).get());
    }

    private static boolean isRealityGear(ItemStack stack) {
        return stack.is(SWORD.get()) || stack.is(HELMET.get()) || stack.is(CHEST.get())
            || stack.is(LEGS.get()) || stack.is(BOOTS.get());
    }

    private static void deny(CommandEvent event, CommandSourceStack source) {
        event.setCanceled(true);
        source.sendFailure(Component.literal("Trang bị Thực Tại kháng lại lệnh này!"));
    }

    private static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        AngelFlight.tick(p);
        if (!p.isAlive()) return;
        AttributeInstance health = p.getAttribute(Attributes.MAX_HEALTH);
        if (!fullSet(p)) {
            // Set incomplete: the bonus health goes away again.
            if (health != null) health.removeModifier(EXTRA_HEALTH_ID);
            return;
        }
        if (health != null && !health.hasModifier(EXTRA_HEALTH_ID)) {
            health.addTransientModifier(new AttributeModifier(EXTRA_HEALTH_ID, 60.0,
                AttributeModifier.Operation.ADD_VALUE));
        }
        p.setHealth(p.getMaxHealth());
        p.clearFire();
        p.setAirSupply(p.getMaxAirSupply());
        p.setTicksFrozen(0);
        // NOTE: fall distance is deliberately NOT zeroed here. Fall damage
        // is already cancelled by protect(), and zeroing it every tick
        // would make critical hits impossible, so the Reality Sword could
        // never release its sword-qi blade while the full set is worn.
        if (p.tickCount % 20 == 0) {
            p.getFoodData().eat(20, 1.0F);
            for (var effect : List.copyOf(p.getActiveEffects())) {
                if (!effect.getEffect().value().isBeneficial()) p.removeEffect(effect.getEffect());
            }
            // Full-set blessings, refreshed every second so they never run
            // out while the set is worn. Everything is at max level:
            // amplifier 255 (level 256) for the scaling effects, except
            // Speed and Jump Boost, which stay at level I so movement
            // stays controllable. Resistance caps at level V (100%
            // reduction), and Fire Resistance, Water Breathing and Night
            // Vision have no levels, so they stay at 0.
            bless(p, MobEffects.REGENERATION, 255);
            bless(p, MobEffects.FIRE_RESISTANCE, 0);
            bless(p, MobEffects.DAMAGE_BOOST, 255);
            bless(p, MobEffects.DAMAGE_RESISTANCE, 4);
            bless(p, MobEffects.WATER_BREATHING, 0);
            bless(p, MobEffects.NIGHT_VISION, 0);
            bless(p, MobEffects.LUCK, 255);
            bless(p, MobEffects.MOVEMENT_SPEED, 0);
            bless(p, MobEffects.JUMP, 0);
            bless(p, MobEffects.DIG_SPEED, 255);
        }
        // Recover above the same column when falling below the world.
        if (p.getY() < p.level().getMinBuildHeight() - 32) {
            int y = Math.max(p.level().getMinBuildHeight() + 4,
                p.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    p.blockPosition().getX(), p.blockPosition().getZ()) + 2);
            p.teleportTo(p.getX(), y, p.getZ());
            p.setDeltaMovement(0, 0, 0);
            p.fallDistance = 0;
        }
    }

    private static void bless(ServerPlayer p, Holder<MobEffect> effect, int amplifier) {
        p.addEffect(new MobEffectInstance(effect, 100, amplifier, false, false, true));
    }
}
