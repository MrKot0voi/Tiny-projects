package com.mrmelon54.DraggableLists;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Cursor {
    private static boolean isDragging;
    private static long vResizeCursor = 0L;
    private static long arrowCursor = 0L;

    public static void setDragging() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getWindow() == null || mc.getWindow().handle() == 0L) {
                return;
            }
            isDragging = true;
            if (vResizeCursor == 0L) {
                vResizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR);
            }
            if (vResizeCursor != 0L) {
                GLFW.glfwSetCursor(mc.getWindow().handle(), vResizeCursor);
            }
        } catch (Throwable t) {
            CrashGuard.report("Cursor.setDragging", t);
        }
    }

    public static void reset() {
        if (!isDragging) return;
        isDragging = false;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getWindow() == null || mc.getWindow().handle() == 0L) {
                return;
            }
            if (arrowCursor == 0L) {
                arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
            }
            GLFW.glfwSetCursor(mc.getWindow().handle(), arrowCursor);
        } catch (Throwable t) {
            CrashGuard.report("Cursor.reset", t);
        }
    }
}
