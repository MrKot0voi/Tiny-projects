package net.uku3lig.totemcounter;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.uku3lig.totemcounter.config.TotemCounterConfig;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static net.minecraft.ChatFormatting.*;

@Mod(value = TotemCounter.MOD_ID, dist = Dist.CLIENT)
public class TotemCounter {
    public static final String MOD_ID = "totemcounter";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<UUID, Integer> POPS = new HashMap<>();
    private static final List<Identifier> CUSTOM_TOTEMS = List.of(
            Identifier.fromNamespaceAndPath("voidtotem", "totem_of_void_undying")
    );

    public static final ItemStack TOTEM = new ItemStack(Items.TOTEM_OF_UNDYING);
    public static final Identifier DEFAULT_TOTEM = Identifier.fromNamespaceAndPath(MOD_ID, "gui/totem.png");
    public static final Identifier WHITE_BAR = Identifier.fromNamespaceAndPath(MOD_ID, "gui/bar.png");

    private static final Component PREFIX = Component.empty()
            .append(Component.literal("Totem").withStyle(YELLOW, BOLD))
            .append(Component.literal("Counter").withStyle(GREEN, BOLD))
            .append(Component.literal(" » ").withStyle(GRAY, BOLD))
            .append(Component.empty().withStyle(RESET));
    private static final Component HEADER = Component.empty()
            .append(Component.literal(" ====== ").withStyle(GRAY))
            .append(Component.literal("Totem").withStyle(YELLOW, BOLD))
            .append(Component.literal("Counter").withStyle(GREEN, BOLD))
            .append(Component.literal(" ====== ").withStyle(GRAY))
            .append(Component.empty().withStyle(RESET));

    public TotemCounter(IEventBus modEventBus, ModContainer modContainer) {
        ClientSetup.initialize(modEventBus, modContainer);
    }

    public static Map<UUID, Integer> getPops() {
        return POPS;
    }

    public static TotemCounterConfig getConfig() {
        return TotemCounterConfig.get();
    }

    public static Component getPrefix() {
        return PREFIX;
    }

    public static Component getHeader() {
        return HEADER;
    }

    public static int getCount(Player player) {
        if (player == null) return 0;
        if (TotemCounterConfig.get().isShowPopCounter()) {
            return POPS.getOrDefault(player.getUUID(), 0);
        }

        Inventory inventory = player.getInventory();
        ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
        return Stream.concat(inventory.getNonEquipmentItems().stream(), Stream.of(offhand))
                .filter(TotemCounter::isTotem)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static boolean isTotem(ItemStack stack) {
        if (stack.is(Items.TOTEM_OF_UNDYING)) return true;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return CUSTOM_TOTEMS.contains(id);
    }

    public static int getColor(int count) {
        TotemCounterConfig config = TotemCounterConfig.get();
        if (!config.isDisplayColors()) return 0xFFFFFFFF;
        return config.isShowPopCounter() ? getPopColor(count) : getTotemColor(count);
    }

    public static int getPopColor(int pops) {
        return switch (pops) {
            case 1, 2 -> 0xFF55FF55;
            case 3, 4 -> 0xFF00AA00;
            case 5, 6 -> 0xFFFFFF55;
            case 7, 8 -> 0xFFFFAA00;
            default -> 0xFFFF5555;
        };
    }

    public static int getTotemColor(int amount) {
        return switch (amount) {
            case 1, 2 -> 0xFFFF5555;
            case 3, 4 -> 0xFFFFAA00;
            case 5, 6 -> 0xFFFFFF55;
            case 7, 8 -> 0xFF00AA00;
            default -> 0xFF55FF55;
        };
    }

    public static Component showPopsInText(Player entity, Component text) {
        TotemCounterConfig config = TotemCounterConfig.get();
        if (POPS.containsKey(entity.getUUID()) && config.isCounterEnabled()) {
            int popCount = POPS.get(entity.getUUID());
            MutableComponent label = text.copy().append(" ");
            MutableComponent counter = Component.literal("-" + popCount);
            if (config.isSeparator()) {
                label.append(Component.literal("| ").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            }
            if (config.isCounterColors()) {
                counter.setStyle(Style.EMPTY.withColor(getPopColor(popCount)));
            }
            label.append(counter);
            return label;
        }
        return text;
    }

    public static void resetPopCounter() {
        POPS.clear();
    }

}
