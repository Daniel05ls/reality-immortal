package vn.dicestion.reality;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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

    public RealityImmortal(IEventBus bus) {
        MATERIALS.register(bus);
        ITEMS.register(bus);
        bus.addListener(RealityImmortal::creativeTab);
        NeoForge.EVENT_BUS.addListener(RealityImmortal::protect);
        NeoForge.EVENT_BUS.addListener(RealityImmortal::tick);
        NeoForge.EVENT_BUS.addListener(RealityCombat::onAttack);
        NeoForge.EVENT_BUS.addListener(SpaceRift::tick);
        NeoForge.EVENT_BUS.addListener(SpaceRift::stop);
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

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            event.accept(SWORD.get());
            event.accept(HELMET.get());
            event.accept(CHEST.get());
            event.accept(LEGS.get());
            event.accept(BOOTS.get());
        }
    }

    public static boolean fullSet(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(HELMET.get())
            && p.getItemBySlot(EquipmentSlot.CHEST).is(CHEST.get())
            && p.getItemBySlot(EquipmentSlot.LEGS).is(LEGS.get())
            && p.getItemBySlot(EquipmentSlot.FEET).is(BOOTS.get());
    }

    private static void protect(LivingIncomingDamageEvent event) {
        // Respect administrative /kill. Do not mutate creative/invulnerable abilities.
        if (event.getEntity() instanceof ServerPlayer p && fullSet(p)
                && !event.getSource().is(DamageTypes.GENERIC_KILL)
                && !event.getSource().is(RealityCombat.REALITY_CUT)) {
            event.setCanceled(true);
        }
    }

    private static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !p.isAlive() || !fullSet(p)) return;
        p.setHealth(p.getMaxHealth());
        p.clearFire();
        p.setAirSupply(p.getMaxAirSupply());
        p.setTicksFrozen(0);
        p.fallDistance = 0;
        if (p.tickCount % 20 == 0) {
            p.getFoodData().eat(20, 1.0F);
            for (var effect : List.copyOf(p.getActiveEffects())) {
                if (!effect.getEffect().value().isBeneficial()) p.removeEffect(effect.getEffect());
            }
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
}
