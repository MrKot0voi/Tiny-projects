package io.github.a5b84.darkloadingscreen.mixin;

import static io.github.a5b84.darkloadingscreen.DarkLoadingScreen.config;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.a5b84.darkloadingscreen.CrashGuard;
import io.github.a5b84.darkloadingscreen.DarkLoadingScreen;
import io.github.a5b84.darkloadingscreen.DrawTextureLambda;
import io.github.a5b84.darkloadingscreen.config.Config;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    @Mutable @Shadow private static @Final int LOGO_BACKGROUND_COLOR;
    @Mutable @Shadow private static @Final int LOGO_BACKGROUND_COLOR_DARK;
    @Mutable @Shadow private static @Final IntSupplier BRAND_BACKGROUND;

    @Unique
    private static Config darkLoadingScreen$cfg() {
        return config != null ? config : Config.DEFAULT;
    }

    @Unique
    private static void darkLoadingScreen$applyBrandBackground() {
        try {
            int bg = ARGB.opaque(darkLoadingScreen$cfg().bg);
            LOGO_BACKGROUND_COLOR = bg;
            LOGO_BACKGROUND_COLOR_DARK = bg;
            BRAND_BACKGROUND = () -> ARGB.opaque(darkLoadingScreen$cfg().bg);
        } catch (Throwable t) {
            CrashGuard.report("LoadingOverlayMixin.applyBrandBackground", t);
        }
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void adjustBg(CallbackInfo ci) {
        darkLoadingScreen$applyBrandBackground();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void forceBrandBackground(GuiGraphics context, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CrashGuard.flushIfReady();
        darkLoadingScreen$applyBrandBackground();
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"))
    private void drawBarBackground(GuiGraphics context, int x1, int y1, int x2, int y2, float opacity, CallbackInfo ci) {
        if (context == null) return;
        try {
            int alpha = Math.round(Math.clamp(opacity, 0.0F, 1.0F) * 255.0F) << 24;
            context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, darkLoadingScreen$cfg().barBg | alpha);
        } catch (Throwable t) {
            CrashGuard.report("LoadingOverlayMixin.drawBarBackground", t);
        }
    }

    @WrapOperation(method = "drawProgressBar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;color(IIII)I"))
    private int modifyBorderColor(int alpha, int r, int g, int b, Operation<Integer> original) {
        int orig = original.call(alpha, r, g, b);
        return darkLoadingScreen$cfg().border | (orig & 0xff000000);
    }

    @WrapOperation(method = "drawProgressBar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0))
    private void drawBarFill(GuiGraphics context, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        original.call(context, x1, y1, x2, y2, darkLoadingScreen$cfg().bar | (color & 0xff000000));
    }

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V"))
    private void onDrawTexture(GuiGraphics context, RenderPipeline originalPipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color, Operation<Void> original) {
        try {
            Config cfg = darkLoadingScreen$cfg();
            int alpha = ARGB.alpha(color);

            DrawTextureLambda drawTexture = (pipeline, r, g, b) -> {
                if (r > 0 || g > 0 || b > 0) {
                    original.call(context, pipeline, sprite, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, ARGB.color(
                            alpha,
                            ARGB.as8BitChannel(Math.max(r, 0)),
                            ARGB.as8BitChannel(Math.max(g, 0)),
                            ARGB.as8BitChannel(Math.max(b, 0))
                    ));
                }
            };

            drawTexture.call(
                    DarkLoadingScreen.MOJANG_LOGO_SHADOWS,
                    cfg.bgR - cfg.logoR,
                    cfg.bgG - cfg.logoG,
                    cfg.bgB - cfg.logoB
            );

            drawTexture.call(
                    originalPipeline,
                    cfg.logoR - cfg.bgR,
                    cfg.logoG - cfg.bgG,
                    cfg.logoB - cfg.bgB
            );
        } catch (Throwable t) {
            CrashGuard.report("LoadingOverlayMixin.onDrawTexture", t);
            original.call(context, originalPipeline, sprite, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, color);
        }
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = DarkLoadingScreen.VANILLA_FADE_IN_DURATION))
    private float getFadeInTime(float old) {
        return darkLoadingScreen$cfg().fadeInMs;
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = DarkLoadingScreen.VANILLA_FADE_OUT_DURATION))
    private float getFadeOutTime(float old) {
        return darkLoadingScreen$cfg().fadeOutMs;
    }
}
