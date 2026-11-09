package nukeduck.armorchroma.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import nukeduck.armorchroma.ArmorChroma;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
   /**
    * Отступ от верхнего ряда сердец до ряда брони.
    * В оригинальном Fabric-моде это было приватное поле vanilla (field_32170),
    * которое нельзя надёжно повесить через @Shadow без маппингов 1.21.11.
    * Значение 10 соответствует высоте иконки vanilla-ряда; сверить с Gui.renderArmor.
    */
   private static final int ARMOR_ROW_OFFSET = 10;

   @Inject(
      method = "renderArmor",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void onBeforeRenderArmor(GuiGraphics context, Player player, int top, int heartRows, int heartRowsSpacing, int left, CallbackInfo info) {
      if (ArmorChroma.config.isEnabled()) {
         info.cancel();
         top -= (heartRows - 1) * heartRowsSpacing + ARMOR_ROW_OFFSET;
         ArmorChroma.GUI.draw(context, left, top);
      }
   }
}
