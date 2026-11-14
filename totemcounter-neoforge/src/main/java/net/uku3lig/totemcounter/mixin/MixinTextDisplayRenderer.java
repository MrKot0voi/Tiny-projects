package net.uku3lig.totemcounter.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.uku3lig.totemcounter.CrashGuard;
import net.uku3lig.totemcounter.TotemCounter;
import net.uku3lig.totemcounter.config.TotemCounterConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public class MixinTextDisplayRenderer {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V",
            at = @At("RETURN"))
    private void totemcounter$injectCounter(Display.TextDisplay entity, TextDisplayEntityRenderState renderState, float partialTick, CallbackInfo ci) {
        if (entity == null || renderState == null || renderState.cachedInfo == null) return;
        try {
            if (!TotemCounterConfig.get().isCounterEnabled()) return;
            if (!(entity.getVehicle() instanceof Player player)) return;

            if (!player.isAlive()) {
                TotemCounter.getPops().remove(player.getUUID());
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null) return;

            List<Display.TextDisplay.CachedLine> lines = renderState.cachedInfo.lines();
            for (int i = 0; i < lines.size(); i++) {
                final Display.TextDisplay.CachedLine line = lines.get(i);
                final MutableComponent lineText = Component.empty();
                line.contents().accept((index, style, codePoint) -> {
                    lineText.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(style));
                    return true;
                });
                final String lineString = lineText.getString();
                if (lineString.isBlank() || !lineString.contains(player.getScoreboardName())) continue;

                final Component modified = TotemCounter.showPopsInText(player, lineText);
                if (modified == lineText) return;

                final FormattedCharSequence modifiedSeq = modified.getVisualOrderText();
                final int newLineWidth = mc.font.width(modified);

                final List<Display.TextDisplay.CachedLine> newLines = new ArrayList<>(lines);
                newLines.set(i, new Display.TextDisplay.CachedLine(modifiedSeq, newLineWidth));

                final int newMaxWidth = newLines.stream()
                        .mapToInt(Display.TextDisplay.CachedLine::width)
                        .max().orElse(renderState.cachedInfo.width());

                renderState.cachedInfo = new Display.TextDisplay.CachedInfo(newLines, newMaxWidth);
                return;
            }
        } catch (Throwable t) {
            CrashGuard.report("MixinTextDisplayRenderer.injectCounter", t);
        }
    }
}
