package smsk.smoothscroll.mixin.Chat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Coerce;
import smsk.smoothscroll.CrashGuard;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Shadow
    private int chatScrollbarPos;

    @Unique
    private float smoothScrollPos;
    @Unique
    private float targetScrollPos;
    @Unique
    private int lastVisibleLines;
    @Unique
    private float smoothMaskHeight;
    @Unique
    private boolean maskInitialized = false;

    @Shadow
    private int getLineHeight() {
        return 0;
    }

    @Shadow
    public int getLinesPerPage() {
        return 0;
    }

    @Shadow
    public double getScale() {
        return 1.0;
    }

    @WrapMethod(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V")
    private void renderH(GuiGraphics graphics, Font font, int mouseX, int mouseY, int ticks, boolean focused, boolean hidden, Operation<Void> operation) {
        CrashGuard.flushIfReady();
        if (SmScCfg.chatSmoothness() == 0.0F && SmScCfg.chatOpeningSmoothness() == 0.0F) {
            operation.call(graphics, font, mouseX, mouseY, ticks, focused, hidden);
            return;
        }

        int lineHeight = Math.max(1, this.getLineHeight());
        if (SmScCfg.chatSmoothness() != 0.0F) {
            this.smoothScrollPos = (this.smoothScrollPos - this.targetScrollPos) * (float) Math.pow(SmScCfg.chatSmoothness(), SmoothSc.getLastFrameDuration()) + this.targetScrollPos;
            if (Math.abs(this.smoothScrollPos - this.targetScrollPos) < 1.0F / lineHeight / 2.0F) {
                this.smoothScrollPos = this.targetScrollPos;
            }

            this.chatScrollbarPos = (int) Math.floor(this.smoothScrollPos);
        }

        float scale = (float) this.getScale();
        int bottom = graphics.guiHeight() - 40;
        int top;
        if (SmScCfg.chatOpeningSmoothness() == 0.0F) {
            top = bottom - (int) Math.ceil((this.getLinesPerPage() + 1) * lineHeight * scale);
        } else {
            float target = (this.lastVisibleLines + 1) * lineHeight * scale;
            if (!this.maskInitialized) {
                this.smoothMaskHeight = target;
                this.maskInitialized = true;
            }

            this.smoothMaskHeight = (this.smoothMaskHeight - target) * (float) Math.pow(SmScCfg.chatOpeningSmoothness(), SmoothSc.getLastFrameDuration()) + target;
            top = bottom - (int) Math.ceil(this.smoothMaskHeight);
        }

        graphics.enableScissor(0, top, graphics.guiWidth(), graphics.guiHeight());
        try {
            operation.call(graphics, font, mouseX, mouseY, ticks, focused, hidden);
        } finally {
            graphics.disableScissor();
        }
    }

    @WrapMethod(method = "forEachLine")
    private int forEachLineWrap(@Coerce Object calculator, @Coerce Object consumer, Operation<Integer> operation) {
        int visible = operation.call(calculator, consumer);
        this.lastVisibleLines = visible;
        return visible;
    }

    @WrapMethod(method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V")
    private void renderAccessWrap(ChatComponent.ChatGraphicsAccess access, int guiHeight, int mouseY, boolean focused, Operation<Void> operation) {
        if (SmScCfg.chatSmoothness() == 0.0F || access == null) {
            operation.call(access, guiHeight, mouseY, focused);
            return;
        }

        int dy = (int) Math.floor(this.getDrawOffset());
        access.updatePose(pose -> pose.translate(0.0F, dy));
        try {
            operation.call(access, guiHeight, mouseY, focused);
        } finally {
            access.updatePose(pose -> pose.translate(0.0F, -dy));
        }
    }

    @WrapMethod(method = "scrollChat")
    private void scrollWrap(int amount, Operation<Void> operation) {
        int lineHeight = Math.max(1, this.getLineHeight());
        float newTarget = this.targetScrollPos + amount / 7.0F * (SmScCfg.chatAmount() != 0.0F ? SmScCfg.chatAmount() / lineHeight : 7.0F);
        this.chatScrollbarPos = (int) Math.ceil(newTarget);
        operation.call(0);
        if (newTarget > this.chatScrollbarPos) {
            newTarget = this.chatScrollbarPos;
        }

        if (newTarget < 0.0F) {
            newTarget = 0.0F;
        }

        this.targetScrollPos = newTarget;
    }

    @WrapMethod(method = "resetChatScroll")
    private void resetScrollWrap(Operation<Void> operation) {
        operation.call();
        this.targetScrollPos = this.chatScrollbarPos;
    }

    @Unique
    private float getDrawOffset() {
        return -(this.chatScrollbarPos - this.smoothScrollPos) * Math.max(1, this.getLineHeight());
    }
}
