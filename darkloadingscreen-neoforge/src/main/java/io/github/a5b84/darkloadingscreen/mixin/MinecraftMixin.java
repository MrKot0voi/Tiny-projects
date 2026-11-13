package io.github.a5b84.darkloadingscreen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Диагностика: пишет в лог, какой overlay и screen ставит игра (например, при F3+T). Строки DLS_DEBUG. */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    private static final Logger LOG = LogManager.getLogger("DLS_DEBUG");

    @Inject(method = "setOverlay", at = @At("HEAD"))
    private void dlsLogOverlay(Overlay overlay, CallbackInfo ci) {
        LOG.info("setOverlay -> {}", overlay == null ? "null" : overlay.getClass().getName());
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void dlsLogScreen(Screen screen, CallbackInfo ci) {
        LOG.info("setScreen -> {}", screen == null ? "null" : screen.getClass().getName());
    }
}
