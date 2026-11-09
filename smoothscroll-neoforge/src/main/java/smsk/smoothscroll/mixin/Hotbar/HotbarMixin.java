package smsk.smoothscroll.mixin.Hotbar;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(value = Gui.class, priority = 999)
public class HotbarMixin {
    private static final String BLIT_SPRITE = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
    private static final String RENDER_SLOT = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V";

    @Unique
    private int slotWidth = 20;
    @Unique
    private int slotCount = 9;
    @Unique
    private int rolloverSpace = 4;
    @Unique
    private boolean masked = false;
    @Unique
    private float smoothSelectorPos = 0.0F;

    @WrapMethod(method = "renderItemHotbar")
    private void renderHotbarWrap(GuiGraphics context, DeltaTracker tickCounter, Operation<Void> operation) {
        this.rolloverSpace = SmScCfg.staticSelector() ? 1 : 4;
        int target = (selectedSlot() - SmoothSc.hotbarRollover * this.slotCount) * this.slotWidth - SmoothSc.hotbarRollover * this.rolloverSpace;
        this.smoothSelectorPos = (float) ((this.smoothSelectorPos - target) * Math.pow(SmScCfg.hotbarSmoothness(), SmoothSc.getLastFrameDuration()) + target);
        if (Math.round(this.smoothSelectorPos) < this.rolloverSpace - this.slotWidth / 2) {
            this.smoothSelectorPos = this.smoothSelectorPos + (this.slotCount * this.slotWidth + this.rolloverSpace);
            SmoothSc.hotbarRollover--;
        } else if (Math.round(this.smoothSelectorPos) > this.rolloverSpace - this.slotWidth / 2 + this.slotWidth * this.slotCount) {
            this.smoothSelectorPos = this.smoothSelectorPos - (this.slotCount * this.slotWidth + this.rolloverSpace);
            SmoothSc.hotbarRollover++;
        }

        operation.call(context, tickCounter);
    }

    // Выделение (второй blitSprite в renderItemHotbar).
    @WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = BLIT_SPRITE, ordinal = 1))
    private void moveSelector(GuiGraphics context, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> operation) {
        int hotbarStart = x - selectedSlot() * this.slotWidth;
        if (SmScCfg.staticSelector()) {
            context.pose().pushMatrix();
            context.pose().translate(-(x - hotbarStart) + this.slotCount / 2 * this.slotWidth, 0.0F);
            operation.call(context, pipeline, texture, x, y, width, height);
            context.pose().popMatrix();
        } else {
            if (Math.round(this.smoothSelectorPos) < 0) {
                this.enableMask(context);
                context.pose().pushMatrix();
                context.pose().translate(this.smoothSelectorPos - (x - hotbarStart) + this.slotCount * this.slotWidth + this.rolloverSpace, 0.0F);
                operation.call(context, pipeline, texture, x, y, width, height);
                context.pose().popMatrix();
            } else if (Math.round(this.smoothSelectorPos) > this.slotWidth * 8) {
                this.enableMask(context);
                context.pose().pushMatrix();
                context.pose().translate(this.smoothSelectorPos - (x - hotbarStart) - this.slotCount * this.slotWidth - this.rolloverSpace, 0.0F);
                operation.call(context, pipeline, texture, x, y, width, height);
                context.pose().popMatrix();
            }

            context.pose().pushMatrix();
            context.pose().translate(this.smoothSelectorPos - (x - hotbarStart), 0.0F);
            operation.call(context, pipeline, texture, x, y, width, height);
            context.pose().popMatrix();
            this.endMask(context);
        }
    }

    // Фон хотбара (первый blitSprite в renderItemHotbar).
    @WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = BLIT_SPRITE, ordinal = 0))
    private void moveHotbar(GuiGraphics context, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> operation) {
        if (!SmScCfg.staticSelector()) {
            operation.call(context, pipeline, texture, x, y, width, height);
            return;
        }

        this.enableMask(context);
        context.pose().pushMatrix();
        context.pose().translate(-this.smoothSelectorPos + this.slotCount / 2 * this.slotWidth, 0.0F);
        operation.call(context, pipeline, texture, x, y, width, height);
        context.pose().popMatrix();
        context.pose().pushMatrix();
        context.pose().translate(-this.smoothSelectorPos + this.slotCount * this.slotWidth + this.rolloverSpace + this.slotCount / 2 * this.slotWidth, 0.0F);
        operation.call(context, pipeline, texture, x, y, width, height);
        context.pose().popMatrix();
        context.pose().pushMatrix();
        context.pose().translate(-this.smoothSelectorPos - this.slotCount * this.slotWidth - this.rolloverSpace + this.slotCount / 2 * this.slotWidth, 0.0F);
        operation.call(context, pipeline, texture, x, y, width, height);
        context.pose().popMatrix();
        this.endMask(context);
    }

    // Предметы хотбара (первый вызов renderSlot в renderItemHotbar — цикл по слотам).
    @WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = RENDER_SLOT, ordinal = 0))
    private void moveItems(Gui gui, GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed, Operation<Void> operation) {
        if (!SmScCfg.staticSelector()) {
            operation.call(gui, context, x, y, tickCounter, player, stack, seed);
            return;
        }

        this.enableMask(context);
        context.pose().pushMatrix();
        context.pose().translate(-this.smoothSelectorPos + this.slotCount / 2 * this.slotWidth, 0.0F);
        operation.call(gui, context, x, y, tickCounter, player, stack, seed);
        context.pose().popMatrix();
        context.pose().pushMatrix();
        context.pose().translate(-this.smoothSelectorPos + this.slotCount * this.slotWidth + this.rolloverSpace + this.slotCount / 2 * this.slotWidth, 0.0F);
        operation.call(gui, context, x, y, tickCounter, player, stack, seed);
        context.pose().popMatrix();
        context.pose().pushMatrix();
        context.pose().translate(-this.smoothSelectorPos - this.slotCount * this.slotWidth - this.rolloverSpace + this.slotCount / 2 * this.slotWidth, 0.0F);
        operation.call(gui, context, x, y, tickCounter, player, stack, seed);
        context.pose().popMatrix();
        this.endMask(context);
    }

    @Unique
    private static int selectedSlot() {
        Player player = Minecraft.getInstance().player;
        return player == null ? 0 : player.getInventory().getSelectedSlot();
    }

    @Unique
    private void enableMask(GuiGraphics context) {
        int x2 = context.guiWidth() / 2 - 91;
        int y2 = context.guiHeight() - 22;
        if (!SmScCfg.staticSelector()) {
            context.enableScissor(x2 - 1, y2 - 1, x2 + 182 + 1, y2 + 22 + 1);
        } else {
            context.enableScissor(x2, y2 - 1, x2 + 182, y2 + 22 + 1);
        }

        this.masked = true;
    }

    @Unique
    private void endMask(GuiGraphics context) {
        if (this.masked) {
            if (SmScCfg.enableMaskDebug()) {
                context.fill(-100, -100, context.guiWidth(), context.guiHeight(), (50 << 24) | (255 << 8) | 255);
            }

            context.disableScissor();
            this.masked = false;
        }
    }
}
