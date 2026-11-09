package net.natural.motionblur.config;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    // Variables for handling delayed error messages
    private static boolean delayMessageSent = false;
    private static int tickCounter = 0;
    private static final int TICK_DELAY = 80; // Delay in ticks before showing messages
    private static List<String> errorMessagesToDisplay = new ArrayList<>();
    private static boolean hasConfigReset = false;

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, EventManager::onLoggingIn);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, EventManager::onClientTickPost);
    }

    // Check for config reset and prepare messages when joining a world
    private static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (ConfigManager.isConfigReset()) {
            hasConfigReset = true;
            errorMessagesToDisplay = ConfigManager.getAndClearErrorMessages();
            tickCounter = 0; // Reset the local delay state
            delayMessageSent = false;
        } else {
            hasConfigReset = false;
            errorMessagesToDisplay.clear();
        }
    }

    // Display error messages after a delay
    private static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        // Only process if there are messages to display and they haven't been sent yet
        if (hasConfigReset && !delayMessageSent && client.player != null) {
            if (tickCounter >= TICK_DELAY) {
                // Display accumulated error messages
                for (String errorMessage : errorMessagesToDisplay) {
                    client.player.displayClientMessage(Component.literal("§c" + errorMessage), false);
                }
                // Mark messages as sent for this join
                delayMessageSent = true;
                errorMessagesToDisplay.clear();
                hasConfigReset = false;
            } else {
                tickCounter++;
            }
        }
    }
}
