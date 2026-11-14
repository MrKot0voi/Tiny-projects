package smsk.smoothscroll.mixin.Widgets;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(AbstractScrollArea.class)
public abstract class ScrollableWidgetMixin extends AbstractWidget {
    @Shadow
    private double scrollAmount;

    @Unique
    private double smoothScrollPos;
    @Unique
    private double targetScrollPos;
    @Unique
    private boolean noSetScrollT = false;

    public ScrollableWidgetMixin() {
        super(0, 0, 0, 0, null);
    }

    @Shadow
    public abstract void setScrollAmount(double amount);

    @Inject(method = "setScrollAmount", at = @At("TAIL"))
    private void setScrollT(double amount, CallbackInfo ci) {
        if (!this.noSetScrollT) {
            this.targetScrollPos = this.scrollAmount;
            this.smoothScrollPos = this.scrollAmount;
        }
    }

    @Inject(method = "renderScrollbar", at = @At("HEAD"), require = 0)
    private void updateScroll(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmScCfg.entryListSmoothness() != 0.0F) {
            this.smoothScrollPos = (this.smoothScrollPos - this.targetScrollPos) * Math.pow(SmScCfg.entryListSmoothness(), SmoothSc.getLastFrameDuration()) + this.targetScrollPos;
            this.scrollAmount = Math.round(this.smoothScrollPos);
            this.noSetScrollT = true;
            this.setScrollAmount(this.scrollAmount);
            this.noSetScrollT = false;
        }
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean mouseScrolledWrap(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> operation) {
        this.noSetScrollT = true;
        double prevScrollPos = this.scrollAmount;
        if (SmScCfg.entryListSmoothness() != 0.0F) {
            this.setScrollAmount(this.targetScrollPos);
        }

        boolean ret = operation.call(mouseX, mouseY, horizontalAmount, verticalAmount);
        if (SmScCfg.entryListAmount() != 0.0 && ret) {
            this.setScrollAmount(this.targetScrollPos - SmScCfg.entryListAmount() * verticalAmount);
        }

        this.targetScrollPos = this.scrollAmount;
        if (SmScCfg.entryListSmoothness() != 0.0F) {
            this.setScrollAmount(prevScrollPos);
        }

        this.noSetScrollT = false;
        return ret;
    }
}
