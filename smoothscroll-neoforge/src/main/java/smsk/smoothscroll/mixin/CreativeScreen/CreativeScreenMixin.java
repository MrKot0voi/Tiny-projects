package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

// Сглаживание прокрутки креативного меню: скролл плавно доходит до нужного ряда.
// Скольжение рядов: предметы сдвигает CreativeSlotsMixin, фон сетки (запечён в текстуре вкладки)
// перерисовывается здесь со тем же сдвигом, чтобы тайлы ехали вместе с предметами.
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeScreenMixin {
    /** Размер текстуры вкладки креатива (та же, что у фона экрана). */
    @Unique
    private static final int TAB_TEXTURE_SIZE = 256;

    @Shadow
    private float scrollOffs;

    @Shadow
    private static CreativeModeTab selectedTab;

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

    /**
     * Фон сетки: после штатной отрисовки фона перерисовываем область сетки текстурой вкладки,
     * сдвинутой на тот же dy, что и предметы. Повторы на +-18 px закрывают полосу у края сетки.
     * Вызывается до translate(leftPos, topPos), поэтому координаты здесь экранные.
     */
    @WrapMethod(method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V")
    private void renderBgWrap(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, Operation<Void> operation) {
        operation.call(graphics, partialTick, mouseX, mouseY);

        float dy = SmoothSc.creativeSlideOffset;
        if (dy == 0.0F || SmScCfg.creativeScreenSmoothness() == 0.0F || selectedTab == null) {
            return;
        }

        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) (Object) this;
        int x0 = acc.smoothscroll$getLeftPos() + CreativeSlotsMixin.GRID_X0;
        int y0 = acc.smoothscroll$getTopPos() + CreativeSlotsMixin.GRID_Y0;
        int w = CreativeSlotsMixin.GRID_X1 - CreativeSlotsMixin.GRID_X0;
        int h = CreativeSlotsMixin.GRID_Y1 - CreativeSlotsMixin.GRID_Y0;

        Identifier tab = selectedTab.getBackgroundTexture();
        graphics.enableScissor(x0, y0, x0 + w, y0 + h);
        for (int k = -1; k <= 1; k++) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0.0F, dy + k * 18.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, tab, x0, y0, (float) CreativeSlotsMixin.GRID_X0, (float) CreativeSlotsMixin.GRID_Y0, w, h, TAB_TEXTURE_SIZE, TAB_TEXTURE_SIZE);
            graphics.pose().popMatrix();
        }
        graphics.disableScissor();
    }

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
