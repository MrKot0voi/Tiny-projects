package net.natural.motionblur.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class KeybindingManager {
    private static KeyMapping toggleKeybinding;

    // Called on the mod bus: the key must be registered before the options screen is built
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        toggleKeybinding = new KeyMapping(
                "Toggle Motion Blur",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                KeyMapping.Category.MISC
        );
        event.register(toggleKeybinding);
    }

    // Check for keybinding presses every tick
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        if (toggleKeybinding != null && toggleKeybinding.consumeClick()) {
            toggleMotionBlur();
        }
    }

    private static void toggleMotionBlur() {
        ConfigEntries config = ConfigManager.getConfig();
        config.enabled = !config.enabled;
        ConfigManager.saveConfig();
        if (config.enabled) {
            net.natural.motionblur.ShaderManager.invalidate();
        }
    }
}
