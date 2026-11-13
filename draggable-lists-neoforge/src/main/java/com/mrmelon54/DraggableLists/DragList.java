package com.mrmelon54.DraggableLists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

public interface DragList<T, E extends ObjectSelectionList.Entry<?>> {
    DragItem<T, E> draggable_lists$getEntryAtPosition(double mouseX, double mouseY);

    int draggable_lists$getIndexOfEntry(DragItem<T, E> item);

    void draggable_lists$setDragging(boolean isDragging);

    int draggable_lists$getHeaderHeight();

    int draggable_lists$getY();

    int draggable_lists$getBottom();

    int draggable_lists$getItemHeight();

    int draggable_lists$getRowTop(int index);

    int draggable_lists$getRowBottom(int index);

    double draggable_lists$getRowLeft();

    int draggable_lists$getRowWidth();

    double draggable_lists$getScrollAmount();

    void draggable_lists$setScrollAmount(double scrollAmount);

    void draggable_lists$moveEntry(DragItem<T, E> item, int index);

    int draggable_lists$getItemCount();

    void draggable_lists$renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height);
}
