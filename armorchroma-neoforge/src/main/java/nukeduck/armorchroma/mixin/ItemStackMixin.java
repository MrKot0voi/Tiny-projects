package nukeduck.armorchroma.mixin;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import nukeduck.armorchroma.ArmorChroma;
import nukeduck.armorchroma.CrashGuard;
import nukeduck.armorchroma.MaterialHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
   @Shadow
   public abstract Item getItem();

   @Inject(
      method = "getTooltipLines",
      at = @At("RETURN")
   )
   private void onGetTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> info) {
      try {
         if (ArmorChroma.config.showMaterialInTooltip()) {
            Item item = this.getItem();
            if (item != null) {
               String material = MaterialHelper.getMaterial(item);
               List<Component> lines = info.getReturnValue();
               if (material != null && lines != null) {
                  lines.add(Component.translatable("armorchroma.tooltip.material", material).withStyle(ChatFormatting.GRAY));
               }
            }
         }
      } catch (Throwable t) {
         CrashGuard.report("ItemStackMixin.onGetTooltip", t);
      }
   }
}
