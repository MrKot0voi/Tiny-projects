package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import smsk.smoothscroll.CreativeGrid;
import smsk.smoothscroll.SmoothSc;

/**
 * Скольжение рядов креатива только для сетки предметов.
 * Раньше сдвигался весь renderSlots, поэтому вместе с сеткой уезжал хотбар и предметы выходили за границы.
 * Теперь: сдвиг применяется к каждому слоту сетки отдельно, а вывод обрезается прямоугольником сетки.
 * Координаты локальные: при renderSlot pose уже смещён на (leftPos, topPos).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class CreativeSlotsMixin {
    @WrapMethod(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V")
    private void renderSlotWrap(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, Operation<Void> operation) {
        boolean creativeGridSlot = ((Object) this) instanceof CreativeModeInventoryScreen
                && slot.y < CreativeGrid.SLOT_Y_LIMIT;

        if (!creativeGridSlot || SmoothSc.creativeSlideOffset == 0.0F) {
            operation.call(graphics, slot, mouseX, mouseY);
            return;
        }

        graphics.enableScissor(CreativeGrid.X0, CreativeGrid.Y0, CreativeGrid.X1, CreativeGrid.Y1);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, SmoothSc.creativeSlideOffset);
        operation.call(graphics, slot, mouseX, mouseY);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }
}
