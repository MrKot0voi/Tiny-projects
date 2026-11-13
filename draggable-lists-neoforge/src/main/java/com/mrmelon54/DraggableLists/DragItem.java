package com.mrmelon54.DraggableLists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

public interface DragItem<T, E extends ObjectSelectionList.Entry<E>> {
    T draggable_lists$getUnderlyingData();

    E draggable_lists$getUnderlyingEntry();

    void draggable_lists$render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick);

    void draggable_lists$setBeingDragged(boolean isBeingDragged);
}
