package net.natural.motionblur.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.natural.motionblur.CrashGuard;

public final class GpuBufferUtil {

    private static final int UBO_USAGE = 130;
    private static Method createBufferMethod = null;
    private static boolean usesIntSize = false;

    private GpuBufferUtil() {}

    public static GpuBuffer createUBO(String debugName, int sizeBytes) {
        Object device = RenderSystem.getDevice();
        Supplier<String> label = () -> "naturalmotionblur:" + debugName;
        try {
            if (createBufferMethod == null) {
                try {
                    createBufferMethod = device.getClass().getMethod(
                            "createBuffer", Supplier.class, int.class, long.class);
                    usesIntSize = false;
                } catch (NoSuchMethodException ignored) {
                    createBufferMethod = device.getClass().getMethod(
                            "createBuffer", Supplier.class, int.class, int.class);
                    usesIntSize = true;
                }
            }
            Object sizeArg = usesIntSize ? Integer.valueOf(sizeBytes) : Long.valueOf(sizeBytes);
            return (GpuBuffer) createBufferMethod.invoke(device, label, UBO_USAGE, sizeArg);
        } catch (Throwable e) {
            CrashGuard.report("GpuBufferUtil.createUBO(" + debugName + ")", e);
            return null;
        }
    }

    public static void closeQuietly(GpuBuffer buffer) {
        if (buffer == null) return;
        try {
            if (RenderSystem.isOnRenderThread()) {
                buffer.close();
            }
        } catch (Throwable ignored) {
        }
    }

    public static boolean isClosedBufferException(RuntimeException e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("closed");
    }
}
