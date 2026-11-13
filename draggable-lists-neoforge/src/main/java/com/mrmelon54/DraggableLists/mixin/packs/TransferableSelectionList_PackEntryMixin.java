package com.mrmelon54.DraggableLists.mixin.packs;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mrmelon54.DraggableLists.DragItem;
import com.mrmelon54.DraggableLists.DraggableLists;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class TransferableSelectionList_PackEntryMixin extends ObjectSelectionList.Entry<TransferableSelectionList.Entry> implements DragItem<PackSelectionModel.Entry, TransferableSelectionList.PackEntry> {
    @Shadow
    @Final
    private PackSelectionModel.Entry pack;

    @Unique
    private boolean draggable_lists$isBeingDragged = false;

    @Shadow
    public abstract void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick);

    @WrapWithCondition(
        method = "renderContent(Lnet/minecraft/client/gui/GuiGraphics;IIZF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
        )
    )
    public boolean removeOnUpArrowButtons(GuiGraphics guiGraphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        if (DraggableLists.CONFIG.disableResourcePackArrows && sprite.toString().contains("move_")) {
            return false;
        }
        return true;
    }

    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
    public void removeMoveTowardEnd(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!DraggableLists.CONFIG.disableResourcePackArrows) return;
        if (this.pack.canSelect()) return;
        double relX = event.x() - (double) this.getContentX();
        if (relX < 16.0 || relX > 32.0) return;
        cir.setReturnValue(Boolean.TRUE);
    }

    @Override
    public PackSelectionModel.Entry draggable_lists$getUnderlyingData() {
        return this.pack;
    }

    @Override
    public TransferableSelectionList.PackEntry draggable_lists$getUnderlyingEntry() {
        return (TransferableSelectionList.PackEntry) (Object) this;
    }

    @Override
    public void draggable_lists$render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
        if (!this.draggable_lists$isBeingDragged) return;
        int oldX = this.getX();
        int oldY = this.getY();
        this.setX(left);
        this.setY(top);
        this.renderContent(guiGraphics, mouseX, mouseY, hovered, partialTick);
        this.setX(oldX);
        this.setY(oldY);
    }

    @Override
    public void draggable_lists$setBeingDragged(boolean isBeingDragged) {
        this.draggable_lists$isBeingDragged = isBeingDragged;
    }
}
