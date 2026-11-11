package io.github.a5b84.darkloadingscreen;

import java.lang.reflect.Field;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Тест для CI (ветка selftest-ukus-dls): проверяет, что миксин подменил цвет фона загрузочного экрана. */
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
            Field f = LoadingOverlay.class.getDeclaredField("BRAND_BACKGROUND");
            f.setAccessible(true);
            int actual = ((IntSupplier) f.get(null)).getAsInt();
            int expected = DarkLoadingScreen.config.bg;
            LOG.info("SELFTEST config.bg={}", String.format("%06x", expected));
            LOG.info("SELFTEST BRAND_BACKGROUND actual={}", String.format("%06x", actual));
            LOG.info("SELFTEST RESULT bg_mixin_applied={}", actual == expected);
        } catch (Throwable t) {
            LOG.error("SELFTEST FAIL darkloadingscreen", t);
        }
        Minecraft.getInstance().stop();
    }
}
