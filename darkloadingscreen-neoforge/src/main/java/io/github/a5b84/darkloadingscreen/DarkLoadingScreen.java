package io.github.a5b84.darkloadingscreen;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import io.github.a5b84.darkloadingscreen.config.Config;
import io.github.a5b84.darkloadingscreen.mixin.GlCommandEncoderMixin;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@Mod(value = DarkLoadingScreen.MOD_ID, dist = Dist.CLIENT)
public class DarkLoadingScreen {

    public static final String MOD_ID = "dark_loading_screen";

    /**
     * Vanilla fade in/out durations, in ms.
     * @see net.minecraft.client.gui.screens.LoadingOverlay#render
     */
    public static final float
            VANILLA_FADE_IN_DURATION = 500,
            VANILLA_FADE_OUT_DURATION = 1000;

    /**
     * Render pipeline for the part of the Mojang logo that are darker than the background.
     * Регистрируется через {@link RegisterRenderPipelinesEvent}.
     *
     * @see GlCommandEncoderMixin
     */
    public static final RenderPipeline MOJANG_LOGO_SHADOWS = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/mojang_logo_shadows"))
            .withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE))
            .build();

    /**
     * Значения по умолчанию до загрузки конфига. ModConfigSpec.get() до загрузки бросает исключение,
     * поэтому реальные значения читаются в {@link #onConfigChanged} (ModConfigEvent).
     */
    public static Config config = Config.DEFAULT;

    public DarkLoadingScreen(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        modBus.addListener(DarkLoadingScreen::onRegisterRenderPipelines);
        modBus.addListener(DarkLoadingScreen::onConfigChanged);
    }

    private static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(MOJANG_LOGO_SHADOWS);
    }

    private static void onConfigChanged(ModConfigEvent event) {
        config = Config.read();
    }
}
