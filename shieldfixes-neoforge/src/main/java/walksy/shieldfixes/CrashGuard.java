package walksy.shieldfixes;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CrashGuard {
    private static final Logger LOGGER = LogManager.getLogger("ShieldFixes/CrashGuard");
    private static final Map<String, Long> LAST_WARN_MS = new ConcurrentHashMap<>();
    private static final Queue<String> PENDING_CHAT = new ConcurrentLinkedQueue<>();
    private static final long COOLDOWN_MS = 15_000L;

    private CrashGuard() {
    }

    public static void report(String location, Throwable t) {
        String errType = t != null ? t.getClass().getSimpleName() : "UnknownError";
        String errMsg = (t != null && t.getMessage() != null && !t.getMessage().isBlank()) ? t.getMessage() : errType;
        String key = location + ":" + errType;
        long now = System.currentTimeMillis();
        Long last = LAST_WARN_MS.get(key);
        if (last != null && (now - last) < COOLDOWN_MS) {
            return;
        }
        LAST_WARN_MS.put(key, now);
        LOGGER.error("[ShieldFixes] Prevented critical crash in {}: {}", location, errMsg, t);
        sendOrQueue("§c[ShieldFixes] §fПредотвращён краш в §e" + location + "§f: §7" + errMsg);
    }

    public static void warn(String location, String message) {
        String key = location + ":" + message;
        long now = System.currentTimeMillis();
        Long last = LAST_WARN_MS.get(key);
        if (last != null && (now - last) < COOLDOWN_MS) {
            return;
        }
        LAST_WARN_MS.put(key, now);
        LOGGER.warn("[ShieldFixes] Critical check triggered in {}: {}", location, message);
        sendOrQueue("§e[ShieldFixes] §fПроверка безопасности в §e" + location + "§f: §7" + message);
    }

    private static void sendOrQueue(String chatMessage) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.isSameThread() && mc.player != null && mc.gui != null && mc.gui.getChat() != null) {
                flushPending(mc);
                mc.gui.getChat().addMessage(Component.literal(chatMessage));
            } else if (PENDING_CHAT.size() < 32) {
                PENDING_CHAT.offer(chatMessage);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void flushIfReady() {
        try {
            if (PENDING_CHAT.isEmpty()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.isSameThread() && mc.player != null && mc.gui != null && mc.gui.getChat() != null) {
                flushPending(mc);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void flushPending(Minecraft mc) {
        String msg;
        while ((msg = PENDING_CHAT.poll()) != null) {
            mc.gui.getChat().addMessage(Component.literal(msg));
        }
    }
}
