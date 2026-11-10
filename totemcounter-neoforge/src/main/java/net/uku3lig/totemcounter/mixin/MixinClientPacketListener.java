package net.uku3lig.totemcounter.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.uku3lig.totemcounter.TotemCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Считает тотемы удачи у игроков. Раньше цель была INVOKE на ParticleEngine.createTrackingEmitter,
 * но в 1.21.11 этот вызов не находится внутри handleEntityEvent (краш "Scanned 0 target").
 * Теперь перехватывается вход в handleEntityEvent, а пакет фильтруется по коду события тотема (35).
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    /** Код события "тотем бессмертия сработал" в ClientboundEntityEventPacket */
    private static final byte TOTEM_OF_UNDYING_EVENT = 35;

    @Shadow private ClientLevel level;

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    public void updateCounter(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        if (packet.getEventId() != TOTEM_OF_UNDYING_EVENT) {
            return;
        }

        Entity entity = packet.getEntity(level);
        if (entity instanceof RemotePlayer player) {
            UUID uuid = player.getUUID();
            TotemCounter.getPops().compute(uuid, (u, i) -> i == null ? 1 : i + 1);
        }
    }
}
