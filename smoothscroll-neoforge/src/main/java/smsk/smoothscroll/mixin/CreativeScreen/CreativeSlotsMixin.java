package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import smsk.smoothscroll.SmoothSc;

// Скольжение рядов креатива: сдвигаем слоты на дробную часть прокрутки (внутри renderSlots).
@Mixin(AbstractContainerScreen.class)
public abstract class CreativeSlotsMixin {
    @WrapMethod(method = "renderSlots")
    private void renderSlotsWrap(GuiGraphics graphics, int mouseX, int mouseY, Operation<Void> operation) {
        if (((Object) this) instanceof CreativeModeInventoryScreen && SmoothSc.creativeSlideOffset != 0.0F) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0.0F, SmoothSc.creativeSlideOffset);
            operation.call(graphics, mouseX, mouseY);
            graphics.pose().popMatrix();
        } else {
            operation.call(graphics, mouseX, mouseY);
        }
    }
}
