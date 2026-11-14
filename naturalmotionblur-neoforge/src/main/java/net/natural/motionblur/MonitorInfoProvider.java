package net.natural.motionblur;

import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public class MonitorInfoProvider {

    private static long lastMonitorHandle = 0;
    private static int  lastRefreshRate   = 60;
    private static long lastCheckTime     = 0;
    private static final long CHECK_INTERVAL_NS = 1_000_000_000L;

    public static void updateDisplayInfo() {
        long now = System.nanoTime();
        if (now - lastCheckTime < CHECK_INTERVAL_NS) return;
        lastCheckTime = now;

        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.getWindow() == null) return;

            long window = client.getWindow().handle();
            if (window == 0L) return;

            long monitor = GLFW.glfwGetWindowMonitor(window);
            if (monitor == 0L) {
                monitor = getMonitorFromWindowPosition(window, client.getWindow().getScreenWidth(), client.getWindow().getScreenHeight());
            }

            if (monitor != 0L && monitor != lastMonitorHandle) {
                lastRefreshRate   = detectRefreshRate(monitor);
                lastMonitorHandle = monitor;
            }
        } catch (Throwable t) {
            CrashGuard.report("MonitorInfoProvider.updateDisplayInfo", t);
        }
    }

    public static int getRefreshRate() {
        return lastRefreshRate > 0 ? lastRefreshRate : 60;
    }

    private static long getMonitorFromWindowPosition(long window, int windowWidth, int windowHeight) {
        if (window == 0L) return 0L;
        int[] winX = new int[1], winY = new int[1];
        GLFW.glfwGetWindowPos(window, winX, winY);

        int centerX = winX[0] + windowWidth  / 2;
        int centerY = winY[0] + windowHeight / 2;

        long result = GLFW.glfwGetPrimaryMonitor();
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors != null) {
            for (int i = 0; i < monitors.limit(); i++) {
                long m = monitors.get(i);
                if (m == 0L) continue;
                int[] mx = new int[1], my = new int[1];
                GLFW.glfwGetMonitorPos(m, mx, my);
                GLFWVidMode mode = GLFW.glfwGetVideoMode(m);
                if (mode == null) continue;
                if (centerX >= mx[0] && centerX < mx[0] + mode.width() &&
                        centerY >= my[0] && centerY < my[0] + mode.height()) {
                    result = m;
                    break;
                }
            }
        }
        return result;
    }

    private static int detectRefreshRate(long monitor) {
        if (monitor == 0L) return 60;
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
        return (vidMode != null && vidMode.refreshRate() > 0) ? vidMode.refreshRate() : 60;
    }
}
