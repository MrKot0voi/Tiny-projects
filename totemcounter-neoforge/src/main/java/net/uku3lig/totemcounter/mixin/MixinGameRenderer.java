package net.uku3lig.totemcounter.mixin;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.uku3lig.totemcounter.CrashGuard;
import net.uku3lig.totemcounter.TotemCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "displayItemActivation", at = @At("HEAD"))
    public void updateClientCounter(ItemStack floatingItem, CallbackInfo ci) {
        if (minecraft == null || minecraft.player == null || floatingItem == null) return;
        try {
            if (floatingItem.is(Items.TOTEM_OF_UNDYING)) {
                UUID uuid = minecraft.player.getUUID();
                TotemCounter.getPops().compute(uuid, (u, i) -> i == null ? 1 : i + 1);
            }
        } catch (Throwable t) {
            CrashGuard.report("MixinGameRenderer.updateClientCounter", t);
        }
    }
}
