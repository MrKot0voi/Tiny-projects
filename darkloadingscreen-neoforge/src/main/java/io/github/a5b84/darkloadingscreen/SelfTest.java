package io.github.a5b84.darkloadingscreen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CI-тест (ветка selftest-reload): повторяет F3+T (reloadResourcePacks) и логирует, какой overlay
 * реально появляется и какой у него фон. Всё через рефлексию, чтобы не зависеть от точных имён.
 */
final class SelfTest {
    private static final Logger LOG = LogManager.getLogger("SELFTEST");
    private static int ticks = 0;
    private static int reloadAt = -1;
    private static String lastOverlay = "";
    private static boolean done = false;

    private SelfTest() {
    }

    static void onTick(ClientTickEvent.Post event) {
        if (done) {
            return;
        }
        ticks++;
        Minecraft mc = Minecraft.getInstance();
        if (ticks == 200) {
            try {
                Method m = Minecraft.class.getMethod("reloadResourcePacks");
                m.invoke(mc);
                reloadAt = ticks;
                LOG.info("SELFTEST reloadResourcePacks invoked at tick {}", ticks);
            } catch (Throwable t) {
                LOG.error("SELFTEST FAIL invoke reloadResourcePacks", t);
                finish(mc);
                return;
            }
        }
        if (reloadAt > 0) {
            String name = describeOverlay(mc);
            if (!name.equals(lastOverlay)) {
                LOG.info("SELFTEST tick={} overlay={}", ticks, name);
                lastOverlay = name;
            }
            if (ticks - reloadAt > 400) {
                finish(mc);
            }
        }
        if (ticks > 2000) {
            finish(mc);
        }
    }

    private static String describeOverlay(Minecraft mc) {
        try {
            for (Field f : Minecraft.class.getDeclaredFields()) {
                if (!Overlay.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                f.setAccessible(true);
                Object o = f.get(mc);
                if (o == null) {
                    return "none(field " + f.getName() + ")";
                }
                String s = o.getClass().getName() + "(field " + f.getName() + ")";
                if (o.getClass().getName().endsWith("LoadingOverlay")) {
                    Field bg = o.getClass().getDeclaredField("BRAND_BACKGROUND");
                    bg.setAccessible(true);
                    int v = ((IntSupplier) bg.get(null)).getAsInt();
                    s += " BRAND=" + String.format("%06x", v);
                }
                return s;
            }
            return "no-overlay-field";
        } catch (Throwable t) {
            return "error " + t;
        }
    }

    private static void finish(Minecraft mc) {
        done = true;
        LOG.info("SELFTEST finished at tick {}", ticks);
        mc.stop();
    }
}
