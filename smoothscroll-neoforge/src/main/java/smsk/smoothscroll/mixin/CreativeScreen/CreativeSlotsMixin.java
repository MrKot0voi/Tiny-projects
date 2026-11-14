package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import smsk.smoothscroll.CreativeGrid;
import smsk.smoothscroll.SmoothSc;

/**
 * Скольжение рядов креатива только для сетки предметов.
 * Отрисовка: слоты сетки сдвигаются на dy и обрезаются прямоугольником сетки; хотбар не трогаем.
 * Выбор: наведение и подсветка слота сетки учитывают тот же dy, поэтому курсор попадает в нарисованный предмет.
 * Координаты локальные: при отрисовке слотов и подсветки pose уже смещён на (leftPos, topPos).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class CreativeSlotsMixin {
    @Shadow
    protected Slot hoveredSlot;

    @WrapMethod(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V")
    private void renderSlotWrap(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, Operation<Void> operation) {
        if (!sscIsGridSlot(slot) || SmoothSc.creativeSlideOffset == 0.0F) {
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

    @WrapMethod(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z")
    private boolean isHoveringWrap(Slot slot, double mouseX, double mouseY, Operation<Boolean> operation) {
        if (!sscIsGridSlot(slot) || SmoothSc.creativeSlideOffset == 0.0F) {
            return operation.call(slot, mouseX, mouseY);
        }

        // Курсор должен быть в видимом окне сетки (за ним предметы обрезаны).
        double rx = mouseX - sscLeftPos();
        double ry = mouseY - sscTopPos();
        if (rx < CreativeGrid.X0 || rx >= CreativeGrid.X1 || ry < CreativeGrid.Y0 || ry >= CreativeGrid.Y1) {
            return false;
        }

        // Предмет нарисован на slot.y + dy, поэтому проверяем мышь со сдвигом -dy.
        return operation.call(slot, mouseX, mouseY - SmoothSc.creativeSlideOffset);
    }

    @WrapMethod(method = "renderSlotHighlightBack(Lnet/minecraft/client/gui/GuiGraphics;)V")
    private void highlightBackWrap(GuiGraphics graphics, Operation<Void> operation) {
        sscOffsetHighlight(graphics, operation);
    }

    @WrapMethod(method = "renderSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphics;)V")
    private void highlightFrontWrap(GuiGraphics graphics, Operation<Void> operation) {
        sscOffsetHighlight(graphics, operation);
    }

    @Unique
    private void sscOffsetHighlight(GuiGraphics graphics, Operation<Void> operation) {
        if (!sscIsGridSlot(this.hoveredSlot) || SmoothSc.creativeSlideOffset == 0.0F) {
            operation.call(graphics);
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, SmoothSc.creativeSlideOffset);
        operation.call(graphics);
        graphics.pose().popMatrix();
    }

    @Unique
    private boolean sscIsGridSlot(Slot slot) {
        return ((Object) this) instanceof CreativeModeInventoryScreen
                && slot != null
                && slot.y < CreativeGrid.SLOT_Y_LIMIT;
    }

    @Unique
    private int sscLeftPos() {
        return ((AbstractContainerScreenAccessor) (Object) this).smoothscroll$getLeftPos();
    }

    @Unique
    private int sscTopPos() {
        return ((AbstractContainerScreenAccessor) (Object) this).smoothscroll$getTopPos();
    }
}
