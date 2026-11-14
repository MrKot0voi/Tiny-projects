package fin.starhud;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class CrashGuard {
    private static long lastWarnMs = 0L;

    private CrashGuard() {
    }

    public static void report(Throwable t, String location) {
        try {
            long now = System.currentTimeMillis();
            if (now - lastWarnMs < 15000L) {
                return;
            }
            lastWarnMs = now;
            Main.LOGGER.error("[StarHUD] Prevented critical crash in {}", location, t);
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gui != null) {
                mc.gui.getChat().addMessage(Component.literal("§c[StarHUD] §fПредотвращён сбой в §e" + location + "§f: §7" + t.toString()));
            }
        } catch (Throwable ignored) {
        }
    }
}
