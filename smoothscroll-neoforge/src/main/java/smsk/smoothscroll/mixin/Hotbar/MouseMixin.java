package smsk.smoothscroll.mixin.Hotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Unique
    private int oldSlot;

    @Inject(method = "onScroll", at = @At("HEAD"))
    private void onScrollHead(long window, double horizontal, double vertical, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.oldSlot = player.getInventory().getSelectedSlot();
        }
    }

    @Inject(method = "onScroll", at = @At("TAIL"))
    private void onScrollTail(long window, double horizontal, double vertical, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (SmScCfg.hotbarRollover() && SmScCfg.hotbarSmoothness() != 0.0F && player != null) {
            int newSlot = player.getInventory().getSelectedSlot();
            if (newSlot != this.oldSlot) {
                if (vertical > 0.0) {
                    if (newSlot > this.oldSlot) {
                        SmoothSc.hotbarRollover++;
                    }
                } else if (vertical < 0.0 && newSlot < this.oldSlot) {
                    SmoothSc.hotbarRollover--;
                }
            }
        }
    }
}
