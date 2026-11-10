package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

// Сглаживание прокрутки креативного меню: скролл плавно доходит до нужного ряда.
// Скольжение строк как в оригинале (отрисовка соседнего ряда) пока не портировано.
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeScreenMixin {
    @Shadow
    private float scrollOffs;

    @Unique
    private float smoothScroll;
    @Unique
    private float targetScroll;
    @Unique
    private float lastApplied;
    @Unique
    private boolean initialized = false;

    @WrapMethod(method = "mouseScrolled")
    private boolean scrollWrap(double mouseX, double mouseY, double horizontal, double vertical, Operation<Boolean> operation) {
        if (SmScCfg.creativeScreenSmoothness() == 0.0F) {
            return operation.call(mouseX, mouseY, horizontal, vertical);
        }

        float before = this.scrollOffs;
        boolean ret = operation.call(mouseX, mouseY, horizontal, vertical);
        if (ret) {
            this.targetScroll = this.scrollOffs;
            this.applyScroll(before);
        }

        return ret;
    }

    @WrapMethod(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    private void renderWrap(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Operation<Void> operation) {
        if (SmScCfg.creativeScreenSmoothness() != 0.0F) {
            if (!this.initialized || this.scrollOffs != this.lastApplied) {
                // Скролл изменился извне (смена вкладки, перетаскивание ползунка): синхронизируем.
                this.smoothScroll = this.scrollOffs;
                this.targetScroll = this.scrollOffs;
                this.lastApplied = this.scrollOffs;
                this.initialized = true;
            } else {
                this.smoothScroll = (this.smoothScroll - this.targetScroll) * (float) Math.pow(SmScCfg.creativeScreenSmoothness(), SmoothSc.getLastFrameDuration()) + this.targetScroll;
                if (Math.abs(this.smoothScroll - this.targetScroll) < 1.0E-4F) {
                    this.smoothScroll = this.targetScroll;
                }

                if (this.smoothScroll != this.lastApplied) {
                    this.applyScroll(this.smoothScroll);
                }
            }

            // Дробная часть позиции ряда -> смещение слотов вверх на 18 px на ряд.
            float rows = ((ItemPickerMenuAccessor) this.menu()).invokeCalculateRowCount();
            float rowPos = this.smoothScroll * rows;
            int base = (int) (rowPos + 0.5F);
            SmoothSc.creativeSlideOffset = rows > 0 ? -(rowPos - base) * 18.0F : 0.0F;
        } else {
            SmoothSc.creativeSlideOffset = 0.0F;
        }

        operation.call(graphics, mouseX, mouseY, partialTick);
    }

    // Меню лежит в AbstractContainerScreen (суперкласс), поэтому обращаемся через this с приведением к нему.
    @Unique
    private AbstractContainerScreen<?> screen() {
        return (AbstractContainerScreen<?>) (Object) this;
    }

    @Unique
    private net.minecraft.world.inventory.AbstractContainerMenu menu() {
        return this.screen().getMenu();
    }

    @Unique
    private void applyScroll(float value) {
        this.scrollOffs = value;
        ((CreativeModeInventoryScreen.ItemPickerMenu) this.menu()).scrollTo(value);
        this.lastApplied = value;
    }
}
