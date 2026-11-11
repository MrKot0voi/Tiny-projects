package ru.berdinskiybear.armorhud;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Тест для CI (ветка selftest-ukus-dls): проверяет, что миксин HUD применился и конфиг читается. */
final class SelfTest {
    private static final Logger LOG = LogManager.getLogger("SELFTEST");
    private static int ticks = 0;
    private static boolean done = false;

    private SelfTest() {
    }

    static void onTick(ClientTickEvent.Post event) {
        if (done) {
            return;
        }
        ticks++;
        if (ticks < 200) {
            return;
        }
        done = true;
        try {
            Method m = Gui.class.getDeclaredMethod("drawArmorHud", GuiGraphics.class, DeltaTracker.class);
            LOG.info("SELFTEST drawArmorHud injected into Gui: {}", m != null);
            Path cfg = FMLPaths.CONFIGDIR.get().resolve(ArmorHudMod.MOD_ID + ".json");
            LOG.info("SELFTEST config exists={} path={}", Files.exists(cfg), cfg);
            if (Files.exists(cfg)) {
                LOG.info("SELFTEST config={}", Files.readString(cfg).replaceAll("\\s+", " "));
            }
            LOG.info("SELFTEST RESULT armorhud_mixin_applied={}", m != null);
        } catch (Throwable t) {
            LOG.error("SELFTEST FAIL armorhud", t);
        }
        Minecraft.getInstance().stop();
    }
}
