package io.github.a5b84.darkloadingscreen.mixin;

import static io.github.a5b84.darkloadingscreen.DarkLoadingScreen.config;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.a5b84.darkloadingscreen.DarkLoadingScreen;
import io.github.a5b84.darkloadingscreen.DrawTextureLambda;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    @Mutable @Shadow private static @Final IntSupplier BRAND_BACKGROUND;

    /** Changes the background color */
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void adjustBg(CallbackInfo ci) {
        BRAND_BACKGROUND = () -> config.bg;
    }

    // Progress bar

    /** Renders the bar background and changes the main bar color */
    @ModifyVariable(method = "renderProgressBar",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/ARGB;color(IIII)I"),
            ordinal = 6)
    private int modifyBarColor(int barColor, GuiGraphics context, int x1, int y1, int x2, int y2, float opacity) {
        int alpha = barColor & 0xff000000;
        context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, config.barBg | alpha);
        return config.bar | alpha;
    }

    /** Changes the bar border color */
    @ModifyVariable(method = "renderProgressBar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0, shift = At.Shift.AFTER),
            ordinal = 6)
    private int modifyBarBorderColor(int color) {
        return config.border | color & 0xff000000;
    }

    // Logo

    /** Changes the logo color */
    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V"))
    private void onDrawTexture(GuiGraphics context, RenderPipeline originalPipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color, Operation<Void> original) {
        // `RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)` causes an ugly outline,
        // so we render the logo twice (once for logo channels that are brighter than the background,
        // and another time for those that are darker)

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

        // Order of draws is important because GlCommandEncoderMixin resets the blend equation only
        // when MOJANG_LOGO_SHADOWS is swapped out for something else. One way to ensure this is to
        // draw shadows before highlights.
        drawTexture.call(
                DarkLoadingScreen.MOJANG_LOGO_SHADOWS,
                config.bgR - config.logoR,
                config.bgG - config.logoG,
                config.bgB - config.logoB
        );

        drawTexture.call(
                originalPipeline,
                config.logoR - config.bgR,
                config.logoG - config.bgG,
                config.logoB - config.bgB
        );
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = DarkLoadingScreen.VANILLA_FADE_IN_DURATION))
    private float getFadeInTime(float old) {
        return config.fadeInMs;
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = DarkLoadingScreen.VANILLA_FADE_OUT_DURATION))
    private float getFadeOutTime(float old) {
        return config.fadeOutMs;
    }
}
