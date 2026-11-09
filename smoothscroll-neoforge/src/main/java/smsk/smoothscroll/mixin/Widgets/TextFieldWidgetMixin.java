package smsk.smoothscroll.mixin.Widgets;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin extends AbstractWidget {
    @Shadow
    @Final
    private Font font;
    @Shadow
    private int displayPos;
    @Shadow
    private String value;

    @Unique
    private float smoothScrollPos = 0.0F;
    @Unique
    private float targetScrollPos = 0.0F;
    @Unique
    private float prevCursorPixel = 0.0F;

    public TextFieldWidgetMixin() {
        super(0, 0, 0, 0, null);
    }

    @Shadow
    public abstract int getInnerWidth();

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.isFocused()) {
            return false;
        }

        double max = this.font.width(this.value);
        this.targetScrollPos = (float) Math.max(0.0, Math.min(this.targetScrollPos - (verticalAmount + horizontalAmount) * SmScCfg.textAmount(), max));
        return true;
    }

    @WrapMethod(method = "renderWidget")
    private void renderWidgetWrap(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, Operation<Void> operation) {
        this.smoothScrollPos = (this.smoothScrollPos - this.targetScrollPos) * (float) Math.pow(SmScCfg.textSmoothness(), SmoothSc.getLastFrameDuration()) + this.targetScrollPos;
        this.displayPos = this.font.plainSubstrByWidth(this.value, Math.round(this.smoothScrollPos)).length();
        context.enableScissor(this.getX(), this.getY() - 10, this.getX() + this.getInnerWidth(), this.getY() + 10);
        context.pose().pushMatrix();
        context.pose().translate(this.getDrawOffset(), 0.0F);
        operation.call(context, mouseX, mouseY, deltaTicks);
        context.pose().popMatrix();
        if (SmScCfg.enableMaskDebug()) {
            context.fill(-100, -100, context.guiWidth(), context.guiHeight(), (50 << 24) | (255 << 8) | 255);
        }

        context.disableScissor();
    }

    // Фон поля (первый blitSprite в renderWidget) рисуем без смещения текста.
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void drawBackground(GuiGraphics context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> operation) {
        context.pose().popMatrix();
        context.disableScissor();
        operation.call(context, pipeline, sprite, x, y, width, height);
        context.enableScissor(this.getX(), this.getY() - 10, this.getX() + this.getInnerWidth(), this.getY() + 10);
        context.pose().pushMatrix();
        context.pose().translate(this.getDrawOffset(), 0.0F);
    }

    @ModifyVariable(method = "renderWidget", at = @At("STORE"), ordinal = 0)
    private String addCharacters(String visibleString) {
        if (this.displayPos + visibleString.length() < this.value.length() && visibleString.length() != 0) {
            int firstCharWidth = this.font.width(visibleString.charAt(0) + "");
            String additionalChars = this.font.plainSubstrByWidth(this.value.substring(this.displayPos + visibleString.length()), firstCharWidth);
            int totalLen = this.displayPos + visibleString.length() + additionalChars.length();
            if (this.value.length() > totalLen) {
                additionalChars = additionalChars + this.value.charAt(totalLen);
            }

            return visibleString + additionalChars;
        }

        return visibleString;
    }

    @WrapMethod(method = "findClickedPositionInText")
    private int calculateCursorPosWrap(MouseButtonEvent click, Operation<Integer> operation) {
        return operation.call(new MouseButtonEvent(click.x() - this.getDrawOffset(), click.y(), click.buttonInfo()));
    }

    @WrapMethod(method = "scrollTo")
    private void updateFirstCharacterIndexWrap(int cursor, Operation<Void> operation) {
        if (!SmScCfg.textCustomUpdate()) {
            operation.call(cursor);
            this.targetScrollPos = this.font.width(this.value.substring(0, this.displayPos));
            return;
        }

        int cursorPixel = this.font.width(this.value.substring(0, cursor));
        float innerWidth = this.getInnerWidth();
        float margin = SmScCfg.textMargin() / 100.0F * innerWidth;
        if (SmScCfg.textMargin() > 50.0F) {
            if (cursorPixel < this.prevCursorPixel && cursorPixel < this.targetScrollPos + margin) {
                this.targetScrollPos = cursorPixel - margin;
            } else if (cursorPixel > this.prevCursorPixel && cursorPixel > this.targetScrollPos + innerWidth - margin) {
                this.targetScrollPos = cursorPixel - innerWidth + margin;
            }

            this.targetScrollPos = Math.clamp(this.targetScrollPos, cursorPixel - innerWidth, cursorPixel);
        } else if (cursorPixel < this.targetScrollPos + margin) {
            this.targetScrollPos = cursorPixel - margin;
        } else if (cursorPixel > this.targetScrollPos + innerWidth - margin) {
            this.targetScrollPos = cursorPixel - innerWidth + margin;
        }

        this.targetScrollPos = Math.clamp(this.targetScrollPos, 0.0F, this.font.width(this.value));
        this.displayPos = 0;
        this.prevCursorPixel = cursorPixel;
    }

    @Unique
    private float getDrawOffset() {
        return this.font.width(this.font.plainSubstrByWidth(this.value, Math.round(this.smoothScrollPos))) - this.smoothScrollPos;
    }
}
