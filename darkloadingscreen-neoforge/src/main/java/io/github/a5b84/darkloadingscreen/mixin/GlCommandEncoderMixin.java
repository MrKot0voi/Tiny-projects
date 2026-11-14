package io.github.a5b84.darkloadingscreen.mixin;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.a5b84.darkloadingscreen.CrashGuard;
import io.github.a5b84.darkloadingscreen.DarkLoadingScreen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlCommandEncoder.class)
public abstract class GlCommandEncoderMixin {

    private static final int GL_BLEND_EQUATION = 0x8009;

    @Shadow @Nullable private RenderPipeline lastPipeline;

    @Unique private int oldBlendEquation;

    @Inject(method = "applyPipelineState", at = @At(value = "HEAD"))
    public void onApplyPipelineState(RenderPipeline newPipeline, CallbackInfo ci) {
        try {
            if (lastPipeline != newPipeline) {
                if (newPipeline == DarkLoadingScreen.MOJANG_LOGO_SHADOWS) {
                    oldBlendEquation = GL11.glGetInteger(GL_BLEND_EQUATION);
                    GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
                } else if (lastPipeline == DarkLoadingScreen.MOJANG_LOGO_SHADOWS) {
                    restoreOldBlendEquationIfAny();
                }
            }
        } catch (Throwable t) {
            CrashGuard.report("GlCommandEncoderMixin.onApplyPipelineState", t);
        }
    }

    @Inject(method = "finishRenderPass", at = @At("HEAD"))
    public void onFinishRenderPass(CallbackInfo ci) {
        try {
            restoreOldBlendEquationIfAny();
        } catch (Throwable t) {
            CrashGuard.report("GlCommandEncoderMixin.onFinishRenderPass", t);
        }
    }

    @Unique
    private void restoreOldBlendEquationIfAny() {
        if (oldBlendEquation != 0) {
            GL14.glBlendEquation(oldBlendEquation);
            oldBlendEquation = 0;
        }
    }
}
