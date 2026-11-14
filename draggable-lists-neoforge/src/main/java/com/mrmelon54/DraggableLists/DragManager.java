package com.mrmelon54.DraggableLists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class DragManager<T, E extends ObjectSelectionList.Entry<?>> {
    private final DragList<T, E> dragList;
    private DragItem<T, E> selectedItem;
    private double draggingStartX = 0.0;
    private double draggingStartY = 0.0;
    private double draggingOffsetX = 0.0;
    private double draggingOffsetY = 0.0;
    private double softScrollingTimer = 0.0;
    private double softScrollingOrigin = 0.0;

    public DragManager(DragList<T, E> dragList) {
        this.dragList = dragList;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        try {
            this.dragList.draggable_lists$setDragging(false);
            if (this.selectedItem == null) {
                return false;
            }
            DragItem<T, E> item = this.selectedItem;
            this.selectedItem = null;
            this.softScrollingTimer = 0.0;
            Cursor.reset();
            double cappedY = this.capYCoordinate((int) mouseY, true);
            item.draggable_lists$setBeingDragged(false);
            DragItem<T, E> targetItem = this.dragList.draggable_lists$getEntryAtPosition(mouseX, cappedY);
            if (targetItem == null) {
                return false;
            }
            T srcData = item.draggable_lists$getUnderlyingData();
            T dstData = targetItem.draggable_lists$getUnderlyingData();
            return srcData != null && dstData != null && srcData != dstData && this.dragServerItem(item, cappedY);
        } catch (Throwable t) {
            this.selectedItem = null;
            Cursor.reset();
            CrashGuard.report("DragManager.mouseReleased", t);
            return false;
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        try {
            if (this.selectedItem != null || !this.isCapMouseY((int) mouseY)) {
                return this.selectedItem != null;
            }
            this.selectedItem = this.dragList.draggable_lists$getEntryAtPosition(mouseX, mouseY);
            if (this.selectedItem == null) {
                return false;
            }
            this.draggingStartX = mouseX;
            this.draggingStartY = mouseY;
            this.draggingOffsetX = this.dragList.draggable_lists$getRowLeft() - this.draggingStartX;
            int index = this.dragList.draggable_lists$getIndexOfEntry(this.selectedItem);
            if (index < 0) {
                this.selectedItem = null;
                return false;
            }
            this.draggingOffsetY = (double) this.dragList.draggable_lists$getRowTop(index) - this.draggingStartY;
            if (this.draggingOffsetX > -32.0) {
                this.selectedItem = null;
                return false;
            }
            this.dragList.draggable_lists$setDragging(true);
            E underlying = this.selectedItem.draggable_lists$getUnderlyingEntry();
            if (underlying != null) {
                underlying.setFocused(true);
            }
            this.selectedItem.draggable_lists$setBeingDragged(true);
            this.softScrollingTimer = 0.0;
            Cursor.setDragging();
            return true;
        } catch (Throwable t) {
            this.selectedItem = null;
            Cursor.reset();
            CrashGuard.report("DragManager.mouseDragged", t);
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.selectedItem != null;
    }

    public void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CrashGuard.flushIfReady();
        try {
            int rowLeft = (int) this.dragList.draggable_lists$getRowLeft();
            int rowWidth = this.dragList.draggable_lists$getRowWidth();
            int itemHeight = this.dragList.draggable_lists$getItemHeight() - 4;
            int itemCount = this.dragList.draggable_lists$getItemCount();
            if (itemCount <= 0) {
                return;
            }
            int selectedIndex = this.selectedItem == null ? -1 : this.dragList.draggable_lists$getIndexOfEntry(this.selectedItem);
            int hoveredIndex = this.selectedItem == null ? -1 : this.getIndexFromMouseY(mouseY);

            for (int i = 0; i < itemCount; i++) {
                int shiftedIndex = this.otherItemIndexShift(i, selectedIndex, hoveredIndex);
                int rowTop = this.dragList.draggable_lists$getRowTop(shiftedIndex);
                int rowBottom = this.dragList.draggable_lists$getRowBottom(shiftedIndex);
                if (rowBottom >= this.dragList.draggable_lists$getY() && rowTop <= this.dragList.draggable_lists$getBottom()) {
                    if (i != selectedIndex) {
                        this.dragList.draggable_lists$renderItem(guiGraphics, mouseX, mouseY, partialTick, i, rowLeft, rowTop, rowWidth, itemHeight);
                    }
                }
            }
        } catch (Throwable t) {
            CrashGuard.report("DragManager.renderListItems", t);
        }
    }

    private int otherItemIndexShift(int i, int selectedIndex, int hoveredIndex) {
        if (selectedIndex < 0 || hoveredIndex < 0) {
            return i;
        }
        if (i == selectedIndex) {
            return hoveredIndex;
        }
        if (selectedIndex < hoveredIndex) {
            return (i >= selectedIndex && i <= hoveredIndex) ? i - 1 : i;
        }
        return (i >= hoveredIndex && i <= selectedIndex) ? i + 1 : i;
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.selectedItem == null || guiGraphics == null) {
            return;
        }
        try {
            int rawY = Mth.floor((double) mouseY + this.draggingOffsetY);
            int x = Mth.floor(this.draggingStartX + this.draggingOffsetX);
            int y = this.capYCoordinate(rawY);
            int entryHeight = this.dragList.draggable_lists$getItemHeight() - 4;
            int entryWidth = this.dragList.draggable_lists$getRowWidth();

            guiGraphics.pose().pushMatrix();
            try {
                float c = 0.7490196f;
                guiGraphics.fill(x - 1, y - 1, x + entryWidth - 2, y + entryHeight + 1, ARGB.colorFromFloat(0.5f, c, c, c));
                this.selectedItem.draggable_lists$render(guiGraphics, 0, y, x, entryWidth, entryHeight, mouseX, mouseY, false, partialTick);
            } finally {
                guiGraphics.pose().popMatrix();
            }

            if (y < rawY) {
                if (this.softScrollingTimer == 0.0) {
                    this.softScrollingTimer = (double) Util.getMillis();
                    this.softScrollingOrigin = this.dragList.draggable_lists$getScrollAmount();
                }
                float delta = (float) ((double) Util.getMillis() - this.softScrollingTimer) / 5.0f;
                this.dragList.draggable_lists$setScrollAmount(this.softScrollingOrigin + (double) delta);
            } else if (y > rawY) {
                if (this.softScrollingTimer == 0.0) {
                    this.softScrollingTimer = (double) Util.getMillis();
                    this.softScrollingOrigin = this.dragList.draggable_lists$getScrollAmount();
                }
                float delta = (float) ((double) Util.getMillis() - this.softScrollingTimer) / 5.0f;
                this.dragList.draggable_lists$setScrollAmount(this.softScrollingOrigin - (double) delta);
            } else {
                this.softScrollingTimer = 0.0;
            }
        } catch (Throwable t) {
            CrashGuard.report("DragManager.renderWidget", t);
        }
    }

    private int capYCoordinate(int y, boolean isMouse) {
        int itemH = Math.max(1, this.dragList.draggable_lists$getItemHeight());
        int headerPx = this.dragList.draggable_lists$getHeaderHeight() * itemH;
        int minY = this.dragList.draggable_lists$getY() + 4 + headerPx;
        int maxY = Math.max(minY, this.dragList.draggable_lists$getBottom() + 4 - (2 + (isMouse ? 0 : itemH)));
        return Math.min(Math.max(y, minY), maxY);
    }

    private int capYCoordinate(int y) {
        return this.capYCoordinate(y, false);
    }

    private boolean isCapMouseY(int y) {
        return this.capYCoordinate(y, true) == y;
    }

    private int getIndexFromMouseY(double mouseY) {
        int count = this.dragList.draggable_lists$getItemCount();
        if (count <= 0) {
            return 0;
        }
        int itemH = Math.max(1, this.dragList.draggable_lists$getItemHeight());
        int relY = Mth.floor(mouseY - (double) this.dragList.draggable_lists$getY())
            + (int) this.dragList.draggable_lists$getScrollAmount()
            - 4;
        int idx = relY / itemH;
        int minIdx = Math.min(this.dragList.draggable_lists$getHeaderHeight(), count - 1);
        return Math.max(minIdx, Math.min(idx, count - 1));
    }

    private boolean dragServerItem(DragItem<T, E> item, double mouseY) {
        if (this.dragList.draggable_lists$getItemCount() <= 0) {
            return false;
        }
        int index = this.getIndexFromMouseY(mouseY);
        this.dragList.draggable_lists$moveEntry(item, index);
        return true;
    }
}
