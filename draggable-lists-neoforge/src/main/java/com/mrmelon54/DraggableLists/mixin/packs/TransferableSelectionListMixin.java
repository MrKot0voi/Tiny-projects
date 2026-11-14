package com.mrmelon54.DraggableLists.mixin.packs;

import com.mrmelon54.DraggableLists.CrashGuard;
import com.mrmelon54.DraggableLists.DragItem;
import com.mrmelon54.DraggableLists.DragList;
import com.mrmelon54.DraggableLists.DragManager;
import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.duck.AbstractPackDuckProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@OnlyIn(Dist.CLIENT)
@Mixin(TransferableSelectionList.class)
public abstract class TransferableSelectionListMixin extends ObjectSelectionList<TransferableSelectionList.Entry> implements DragList<PackSelectionModel.Entry, TransferableSelectionList.PackEntry> {
    @Unique
    private final DragManager<PackSelectionModel.Entry, TransferableSelectionList.PackEntry> draggable_lists$dragManager = new DragManager<>(this);

    @Shadow
    protected abstract int scrollBarX();

    public TransferableSelectionListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @Override
    protected void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.draggable_lists$dragManager.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Unique
    private boolean draggable_lists$isMouseOverScrollbar(double mouseX) {
        return this.scrollbarVisible() && mouseX >= (double) this.scrollBarX();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event == null) {
            return this.draggable_lists$dragManager.mouseReleased(0.0, 0.0, 0);
        }
        if (this.draggable_lists$dragManager.mouseReleased(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event != null
            && DraggableLists.CONFIG.resourcePackDraggingEnabled != null
            && DraggableLists.CONFIG.resourcePackDraggingEnabled.isEnabled()
            && !this.draggable_lists$isMouseOverScrollbar(event.x())
            && this.draggable_lists$dragManager.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.draggable_lists$dragManager.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.draggable_lists$dragManager.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DragItem<PackSelectionModel.Entry, TransferableSelectionList.PackEntry> draggable_lists$getEntryAtPosition(double mouseX, double mouseY) {
        AbstractSelectionList.Entry<?> entry = this.getEntryAtPosition(mouseX, mouseY);
        if (entry instanceof TransferableSelectionList.PackEntry) {
            return (DragItem<PackSelectionModel.Entry, TransferableSelectionList.PackEntry>) entry;
        }
        return null;
    }

    @Override
    public int draggable_lists$getIndexOfEntry(DragItem<PackSelectionModel.Entry, TransferableSelectionList.PackEntry> item) {
        return item == null ? -1 : this.children().indexOf(item.draggable_lists$getUnderlyingEntry());
    }

    @Override
    public void draggable_lists$setDragging(boolean isDragging) {
        super.setDragging(isDragging);
    }

    @Override
    public int draggable_lists$getHeaderHeight() {
        return 1;
    }

    @Override
    public int draggable_lists$getY() {
        return this.getY();
    }

    @Override
    public int draggable_lists$getBottom() {
        return this.getBottom();
    }

    @Override
    public int draggable_lists$getItemHeight() {
        return this.defaultEntryHeight;
    }

    @Override
    public int draggable_lists$getRowTop(int index) {
        return this.getRowTop(index);
    }

    @Override
    public int draggable_lists$getRowBottom(int index) {
        return this.getRowBottom(index);
    }

    @Override
    public double draggable_lists$getRowLeft() {
        return (double) this.getRowLeft();
    }

    @Override
    public int draggable_lists$getRowWidth() {
        return this.getRowWidth();
    }

    @Override
    public double draggable_lists$getScrollAmount() {
        return this.scrollAmount();
    }

    @Override
    public void draggable_lists$setScrollAmount(double scrollAmount) {
        this.setScrollAmount(scrollAmount);
    }

    @Override
    public void draggable_lists$moveEntry(DragItem<PackSelectionModel.Entry, TransferableSelectionList.PackEntry> item, int index) {
        if (item != null && item.draggable_lists$getUnderlyingData() instanceof AbstractPackDuckProvider duck) {
            duck.draggable_lists$moveTo(index - 1);
        }
    }

    @Override
    public int draggable_lists$getItemCount() {
        return this.getItemCount();
    }

    @Override
    public void draggable_lists$renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        if (index < 0 || index >= this.children().size()) {
            return;
        }
        TransferableSelectionList.Entry entry = this.children().get(index);
        if (entry == null) {
            return;
        }
        int oldY = entry.getY();
        entry.setY(top);
        try {
            this.renderItem(guiGraphics, mouseX, mouseY, partialTick, entry);
        } catch (Throwable t) {
            CrashGuard.report("TransferableSelectionListMixin.renderItem", t);
        } finally {
            entry.setY(oldY);
        }
    }
}
