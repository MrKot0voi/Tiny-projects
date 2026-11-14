package net.natural.motionblur.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Команды мода. Раньше открывали экран настроек YACL, который в 1.21.11 под NeoForge недоступен,
 * поэтому теперь /motionblur и /mb показывают текущие значения и принимают силу блюра.
 */
public class CommandManager {

    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        registerTree(dispatcher, "motionblur");
        registerTree(dispatcher, "mb");
    }

    private static void registerTree(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .executes(context -> showStatus())
                .then(Commands.argument("strength", FloatArgumentType.floatArg())
                        .executes(context -> setMotionBlurStrength(FloatArgumentType.getFloat(context, "strength")))));
    }

    private static int showStatus() {
        ConfigEntries config = ConfigManager.getConfig();
        sendMessage("Motion blur: " + (config.enabled ? "on" : "off")
                + ", strength " + config.motionBlurStrength + ". Usage: /mb <0-2>");
        return 1;
    }

    private static int setMotionBlurStrength(float strength) {
        if (strength < 0.0f || strength > 2.0f) {
            sendMessage("§cInvalid motion blur strength! Value must be between 0 and 2.");
            return 0;
        }

        ConfigManager.getConfig().motionBlurStrength = strength;
        ConfigManager.saveConfig();
        sendMessage("Motion blur strength set to " + strength);
        return 1;
    }

    private static void sendMessage(String text) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal(text), false);
        }
    }
}
