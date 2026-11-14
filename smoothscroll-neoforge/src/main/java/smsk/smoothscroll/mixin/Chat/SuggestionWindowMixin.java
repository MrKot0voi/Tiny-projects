package smsk.smoothscroll.mixin.Chat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionWindowMixin {
    @Final
    @Shadow
    private Rect2i rect;
    @Shadow
    private int offset;

    @Unique
    private static final int LINE_HEIGHT = 12;
    @Unique
    private boolean initialized = false;
    @Unique
    private float smoothScrollPos;
    @Unique
    private float targetScrollPos;
    @Unique
    private boolean translated;

    @WrapMethod(method = "render")
    private void renderH(GuiGraphics context, int mouseX, int mouseY, Operation<Void> operation) {
        if (SmScCfg.suggestionWindowSmoothness() == 0.0F) {
            operation.call(context, mouseX, mouseY);
            return;
        }

        if (!this.initialized) {
            this.smoothScrollPos = this.offset;
            this.targetScrollPos = this.offset;
            this.initialized = true;
        }

        this.smoothScrollPos = (this.smoothScrollPos - this.targetScrollPos) * (float) Math.pow(SmScCfg.suggestionWindowSmoothness(), SmoothSc.getLastFrameDuration()) + this.targetScrollPos;
        if (Math.abs(this.smoothScrollPos - this.targetScrollPos) < 1.0F / LINE_HEIGHT / 2.0F) {
            this.smoothScrollPos = this.targetScrollPos;
        }

        this.offset = (int) Math.floor(this.smoothScrollPos);
        try {
            operation.call(context, mouseX, (int) Math.floor(mouseY - (int) Math.floor(this.getDrawOffset())));
        } finally {
            this.tryUntranslate(context);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private Object translate(java.util.List<?> list, int index, Operation<Object> operation, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) GuiGraphics context) {
        if (SmScCfg.suggestionWindowSmoothness() != 0.0F && !this.translated && this.rect != null && context != null) {
            context.enableScissor(0, this.rect.getY(), context.guiWidth(), this.rect.getY() + this.rect.getHeight());
            context.pose().pushMatrix();
            context.pose().translate(0.0F, (int) Math.floor(this.getDrawOffset()));
            this.translated = true;
        }

        return operation.call(list, index);
    }

    @Unique
    private void tryUntranslate(GuiGraphics context) {
        if (this.translated && context != null) {
            context.pose().popMatrix();
            if (SmScCfg.enableMaskDebug()) {
                context.fill(-100, -100, context.guiWidth(), context.guiHeight(), (50 << 24) | (255 << 8) | 255);
            }

            context.disableScissor();
        }

        this.translated = false;
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean mScroll(double amount, Operation<Boolean> operation) {
        if (SmScCfg.suggestionWindowSmoothness() == 0.0F) {
            return operation.call(amount);
        }

        int indexBefore = this.offset;
        double newTarget = this.targetScrollPos - amount * (SmScCfg.suggestionWindowAmount() != 0.0F ? SmScCfg.suggestionWindowAmount() / LINE_HEIGHT : 1.0F);
        this.offset = (int) Math.ceil(newTarget);
        boolean ret = operation.call(0.0);
        if (!ret) {
            this.offset = indexBefore;
            return false;
        }

        if (newTarget > this.offset) {
            newTarget = this.offset;
        }

        if (newTarget < 0.0) {
            newTarget = 0.0;
        }

        this.targetScrollPos = (float) newTarget;
        return true;
    }

    @WrapMethod(method = "cycle")
    private void scroll(int offsetAmount, Operation<Void> operation) {
        if (SmScCfg.suggestionWindowSmoothness() == 0.0F) {
            operation.call(offsetAmount);
            return;
        }

        int indexBefore = this.offset;
        this.offset = (int) Math.floor(this.targetScrollPos);
        operation.call(offsetAmount);
        this.targetScrollPos = this.offset;
        this.offset = indexBefore;
    }

    @Unique
    private float getDrawOffset() {
        return (this.offset - this.smoothScrollPos) * LINE_HEIGHT;
    }
}
