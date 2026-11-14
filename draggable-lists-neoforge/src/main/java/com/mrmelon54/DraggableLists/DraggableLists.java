package com.mrmelon54.DraggableLists;

import com.mrmelon54.DraggableLists.config.ConfigStructure;
import java.util.function.Supplier;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.gui.screens.Screen;

public class DraggableLists {
    public static final String MOD_ID = "draggable_lists";
    public static final ConfigStructure CONFIG = initConfigSafe();

    private static ConfigStructure initConfigSafe() {
        try {
            return AutoConfig.register(ConfigStructure.class, JanksonConfigSerializer::new).get();
        } catch (Throwable t) {
            CrashGuard.warn("DraggableLists.initConfigSafe", "Cloth Config недоступен или повреждён, применены стандартные настройки");
            return new ConfigStructure();
        }
    }

    public static void init() {
    }

    public static Supplier<Screen> createConfigScreen(Screen parent) {
        try {
            return AutoConfigClient.getConfigScreen(ConfigStructure.class, parent);
        } catch (Throwable t) {
            CrashGuard.report("DraggableLists.createConfigScreen", t);
            return () -> parent;
        }
    }
}
