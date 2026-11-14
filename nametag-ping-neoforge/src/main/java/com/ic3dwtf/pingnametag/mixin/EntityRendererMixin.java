package com.ic3dwtf.pingnametag.mixin;

import com.ic3dwtf.pingnametag.CrashGuard;
import com.ic3dwtf.pingnametag.config.PingNametagConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void ping_nametag$appendPingOverlayToFinalLabel(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        CrashGuard.flushIfReady();
        if (entity == null || state == null) {
            return;
        }
        try {
            PingNametagConfig config = PingNametagConfig.get();

            if (!config.enabled()) {
                return;
            }

            if (!(entity instanceof AbstractClientPlayer self)) {
                return;
            }

            Component originalText = ((EntityRenderStateAccessor) state).ping_nametag$getDisplayName();
            if (originalText == null || originalText.getString().isEmpty()) {
                return;
            }

            Minecraft client = Minecraft.getInstance();
            if (client == null || client.player == null || client.getConnection() == null) {
                return;
            }

            if (!config.showOwnPing() && self.getUUID().equals(client.player.getUUID())) {
                return;
            }

            PlayerInfo entry = client.getConnection().getPlayerInfo(self.getUUID());
            if (entry == null) {
                String textFormat = config.textFormat().replace("%ping%", "??");
                MutableComponent unknownSuffix = Component.literal(textFormat).setStyle(Style.EMPTY.withColor(0xAAAAAA));
                ((EntityRenderStateAccessor) state).ping_nametag$setDisplayName(
                        ping_nametag$baseLabelWithoutPingSuffix(originalText).append(unknownSuffix)
                );
                return;
            }

            int latency = Math.max(0, entry.getLatency());
            String textFormat = config.textFormat().replace("%ping%", String.valueOf(latency));
            MutableComponent suffix = Component.literal(textFormat)
                    .setStyle(Style.EMPTY.withColor(config.colorForPing(latency)));

            ((EntityRenderStateAccessor) state).ping_nametag$setDisplayName(
                    ping_nametag$baseLabelWithoutPingSuffix(originalText).append(suffix)
            );
        } catch (Throwable t) {
            CrashGuard.report("EntityRendererMixin.appendPingOverlay", t);
        }
    }

    private static MutableComponent ping_nametag$baseLabelWithoutPingSuffix(Component originalText) {
        var siblings = originalText.getSiblings();
        if (siblings.isEmpty()) {
            return originalText.copy();
        }

        Component lastSibling = siblings.get(siblings.size() - 1);
        int limit = ping_nametag$isPingSuffix(lastSibling.getString()) ? siblings.size() - 1 : siblings.size();

        MutableComponent rebuilt = MutableComponent.create(originalText.getContents()).setStyle(originalText.getStyle());
        for (int i = 0; i < limit; i++) {
            rebuilt.append(siblings.get(i));
        }
        return rebuilt;
    }

    private static boolean ping_nametag$isPingSuffix(String text) {
        if (text == null || !text.startsWith(" (") || !text.endsWith("ms)")) {
            return false;
        }

        String value = text.substring(2, text.length() - 3);
        if ("??".equals(value)) {
            return true;
        }

        if (value.isEmpty()) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
