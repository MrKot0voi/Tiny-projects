package smsk.smoothscroll.mixin.CreativeScreen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import smsk.smoothscroll.CrashGuard;
import smsk.smoothscroll.CreativeGrid;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeScreenMixin {
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
        try {
            if (SmScCfg.creativeScreenSmoothness() != 0.0F && this.menu() instanceof ItemPickerMenuAccessor pickerAcc) {
                if (!this.initialized || this.scrollOffs != this.lastApplied) {
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

                float rows = pickerAcc.invokeCalculateRowCount();
                float rowPos = this.smoothScroll * rows;
                int base = (int) (rowPos + 0.5F);
                SmoothSc.creativeSlideOffset = rows > 0 ? -(rowPos - base) * 18.0F : 0.0F;
            } else {
                SmoothSc.creativeSlideOffset = 0.0F;
            }
        } catch (Throwable t) {
            SmoothSc.creativeSlideOffset = 0.0F;
            CrashGuard.report("CreativeScreenMixin.renderWrap", t);
        }

        operation.call(graphics, mouseX, mouseY, partialTick);
    }

    @WrapMethod(method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V")
    private void renderBgWrap(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, Operation<Void> operation) {
        operation.call(graphics, partialTick, mouseX, mouseY);

        float dy = SmoothSc.creativeSlideOffset;
        if (dy == 0.0F || SmScCfg.creativeScreenSmoothness() == 0.0F || selectedTab == null || graphics == null) {
            return;
        }

        Identifier tab = selectedTab.getBackgroundTexture();
        if (tab == null) {
            return;
        }

        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) (Object) this;
        int x0 = acc.smoothscroll$getLeftPos() + CreativeGrid.X0;
        int y0 = acc.smoothscroll$getTopPos() + CreativeGrid.Y0;
        int w = CreativeGrid.X1 - CreativeGrid.X0;
        int h = CreativeGrid.Y1 - CreativeGrid.Y0;

        graphics.enableScissor(x0, y0, x0 + w, y0 + h);
        try {
            for (int k = -1; k <= 1; k++) {
                graphics.pose().pushMatrix();
                try {
                    graphics.pose().translate(0.0F, dy + k * 18.0F);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, tab, x0, y0, (float) CreativeGrid.X0, (float) CreativeGrid.Y0, w, h, TAB_TEXTURE_SIZE, TAB_TEXTURE_SIZE);
                } finally {
                    graphics.pose().popMatrix();
                }
            }
        } finally {
            graphics.disableScissor();
        }
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
        if (this.menu() instanceof CreativeModeInventoryScreen.ItemPickerMenu pickerMenu) {
            pickerMenu.scrollTo(value);
        }
        this.lastApplied = value;
    }
}
