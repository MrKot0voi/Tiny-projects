package net.uku3lig.totemcounter.mixin;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.uku3lig.totemcounter.CrashGuard;
import net.uku3lig.totemcounter.TotemCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    private static final byte TOTEM_OF_UNDYING_EVENT = 35;

    @Shadow private ClientLevel level;

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    public void updateCounter(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        if (packet == null || packet.getEventId() != TOTEM_OF_UNDYING_EVENT) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || !mc.isSameThread() || this.level == null) {
            return;
        }
        try {
            Entity entity = packet.getEntity(this.level);
            if (entity instanceof RemotePlayer player) {
                UUID uuid = player.getUUID();
                TotemCounter.getPops().compute(uuid, (u, i) -> i == null ? 1 : i + 1);
            }
        } catch (Throwable t) {
            CrashGuard.report("MixinClientPacketListener.updateCounter", t);
        }
    }
}
