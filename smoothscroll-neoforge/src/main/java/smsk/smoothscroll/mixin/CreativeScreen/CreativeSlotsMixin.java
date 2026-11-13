package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import smsk.smoothscroll.SmoothSc;

/**
 * Скольжение рядов креатива только для сетки предметов.
 * Раньше сдвигался весь renderSlots, поэтому вместе с сеткой уезжал хотбар и предметы выходили за границы.
 * Теперь: сдвиг применяется к каждому слоту сетки отдельно, а вывод обрезается прямоугольником сетки.
 * Координаты локальные: при renderSlot pose уже смещён на (leftPos, topPos).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class CreativeSlotsMixin {
    /** Прямоугольник сетки (локальные координаты): 9 колонок x 5 рядов, тайлы 18 px. */
    static final int GRID_X0 = 8;
    static final int GRID_Y0 = 17;
    static final int GRID_X1 = 8 + 9 * 18;
    static final int GRID_Y1 = 17 + 5 * 18;
    /** Слоты сетки имеют y < 108; слоты хотбара ниже. */
    static final int GRID_SLOT_Y_LIMIT = 108;

    @WrapMethod(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V")
    private void renderSlotWrap(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, Operation<Void> operation) {
        boolean creativeGridSlot = ((Object) this) instanceof CreativeModeInventoryScreen
                && slot.y < GRID_SLOT_Y_LIMIT;

        if (!creativeGridSlot || SmoothSc.creativeSlideOffset == 0.0F) {
            operation.call(graphics, slot, mouseX, mouseY);
            return;
        }

        graphics.enableScissor(GRID_X0, GRID_Y0, GRID_X1, GRID_Y1);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, SmoothSc.creativeSlideOffset);
        operation.call(graphics, slot, mouseX, mouseY);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }
}
