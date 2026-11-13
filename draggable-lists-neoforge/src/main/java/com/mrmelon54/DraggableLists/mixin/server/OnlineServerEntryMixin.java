package com.mrmelon54.DraggableLists.mixin.server;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mrmelon54.DraggableLists.DragItem;
import com.mrmelon54.DraggableLists.DraggableLists;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class OnlineServerEntryMixin extends ObjectSelectionList.Entry<ServerSelectionList.Entry> implements DragItem<ServerData, ServerSelectionList.OnlineServerEntry> {
    @Shadow
    @Final
    private ServerData serverData;

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
    public boolean removeUpOnButton(GuiGraphics guiGraphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        if (DraggableLists.CONFIG.disableServerArrows && sprite.toString().contains("move_")) {
            return false;
        }
        return true;
    }

    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
    public void removeSwapEntries(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!DraggableLists.CONFIG.disableServerArrows) return;
        double relX = event.x() - (double) this.getContentX();
        if (relX > 16.0) return;
        cir.setReturnValue(Boolean.FALSE);
    }

    @Override
    public ServerData draggable_lists$getUnderlyingData() {
        return this.serverData;
    }

    @Override
    public ServerSelectionList.OnlineServerEntry draggable_lists$getUnderlyingEntry() {
        return (ServerSelectionList.OnlineServerEntry) (Object) this;
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
