package fin.starhud.init;

import com.mojang.blaze3d.platform.InputConstants;
import fin.starhud.Main;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeybindInit {
    public static void init() {
        Main.keyCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("starhud", "category"));
        Main.openEditHUDKey = new KeyMapping(
                "key.starhud.open_edithud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                Main.keyCategory
        );
        Main.toggleHUDKey = new KeyMapping(
                "key.starhud.toggle_hud",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                Main.keyCategory
        );
        Main.toggleInventoryHUDKey = new KeyMapping(
                "key.starhud.toggle_inventory_hud",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                Main.keyCategory
        );
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(Main.openEditHUDKey);
        event.register(Main.toggleHUDKey);
        event.register(Main.toggleInventoryHUDKey);
    }
}
