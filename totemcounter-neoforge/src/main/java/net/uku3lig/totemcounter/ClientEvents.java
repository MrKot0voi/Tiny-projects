package net.uku3lig.totemcounter;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.KeyMapping.Category;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class ClientEvents {
    private static final Category CATEGORY = new Category(Identifier.fromNamespaceAndPath(TotemCounter.MOD_ID, "key"));
    private static final KeyMapping RESET_COUNTER = new KeyMapping(
            "totemcounter.reset", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, CATEGORY
    );

    private ClientEvents() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(RESET_COUNTER);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (RESET_COUNTER.consumeClick()) {
            resetCounterWithMessage();
        }
    }

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("resetcounter")
                .executes(context -> {
                    TotemCounter.resetPopCounter();
                    context.getSource().sendSystemMessage(TotemCounter.getPrefix().copy()
                            .append(Component.translatable("totemcounter.reset.success")));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(getOnlinePlayerNames(), builder))
                        .executes(context -> resetPlayerCounter(context))));

        event.getDispatcher().register(Commands.literal("showpops")
                .executes(context -> showPops(context))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(getOnlinePlayerNames(), builder))
                        .executes(context -> showPlayerPops(context))));
    }

    private static int resetPlayerCounter(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "player");
        Player player = findPlayer(name);
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Player is not currently loaded: " + name));
            return 0;
        }

        TotemCounter.getPops().remove(player.getUUID());
        context.getSource().sendSystemMessage(TotemCounter.getPrefix().copy()
                .append(Component.translatable("totemcounter.reset.player", player.getScoreboardName())
                        .withStyle(style -> style.withColor(net.minecraft.ChatFormatting.GREEN))));
        return Command.SINGLE_SUCCESS;
    }

    private static int showPops(CommandContext<CommandSourceStack> context) {
        if (TotemCounter.getPops().isEmpty()) {
            context.getSource().sendSystemMessage(TotemCounter.getPrefix().copy()
                    .append(Component.translatable("totemcounter.show.noPops")));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendSystemMessage(TotemCounter.getHeader());
        TotemCounter.getPops().forEach((uuid, popCount) -> {
            Player player = Minecraft.getInstance().level == null ? null
                    : Minecraft.getInstance().level.getPlayerByUUID(uuid);
            Component name = (player != null ? player.getDisplayName().copy() : Component.literal(uuid.toString()))
                    .withStyle(net.minecraft.ChatFormatting.DARK_AQUA);
            Component line = name.copy()
                    .append(Component.literal(": ").withStyle(net.minecraft.ChatFormatting.GRAY))
                    .append(Component.literal("-" + popCount)
                            .withStyle(style -> style.withColor(TotemCounter.getPopColor(popCount))));
            context.getSource().sendSystemMessage(line);
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int showPlayerPops(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "player");
        Player player = findPlayer(name);
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Player is not currently loaded: " + name));
            return 0;
        }

        Component playerName = player.getDisplayName().copy().withStyle(net.minecraft.ChatFormatting.DARK_AQUA);
        int popCount = TotemCounter.getPops().getOrDefault(player.getUUID(), 0);
        if (popCount == 0) {
            context.getSource().sendSystemMessage(TotemCounter.getPrefix().copy()
                    .append(Component.translatable("totemcounter.show.player.noPops", playerName)));
        } else {
            Component popText = Component.literal(String.valueOf(popCount))
                    .withStyle(style -> style.withColor(TotemCounter.getPopColor(popCount)));
            context.getSource().sendSystemMessage(TotemCounter.getPrefix().copy()
                    .append(Component.translatable("totemcounter.show.player", playerName, popText)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static List<String> getOnlinePlayerNames() {
        if (Minecraft.getInstance().level == null) return List.of();
        return Minecraft.getInstance().level.players().stream()
                .map(player -> player.getGameProfile().name())
                .toList();
    }

    private static Player findPlayer(String name) {
        if (Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.players().stream()
                .filter(player -> player.getGameProfile().name().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public static void resetCounterWithMessage() {
        TotemCounter.resetPopCounter();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui != null) {
            minecraft.gui.getChat().addMessage(Component.translatable("totemcounter.reset.success"));
        }
    }
}
