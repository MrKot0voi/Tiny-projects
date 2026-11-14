package fin.starhud;

import fin.starhud.config.Settings;
import fin.starhud.init.ConfigInit;
import fin.starhud.init.EventInit;
import fin.starhud.init.KeybindInit;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("starhud");
    public static Settings settings;
    public static KeyMapping.Category keyCategory;
    public static KeyMapping openEditHUDKey;
    public static KeyMapping toggleHUDKey;
    public static KeyMapping toggleInventoryHUDKey;

    public static void init(IEventBus modEventBus) {
        ConfigInit.init();
        KeybindInit.init();
        EventInit.init(modEventBus);
    }
}
