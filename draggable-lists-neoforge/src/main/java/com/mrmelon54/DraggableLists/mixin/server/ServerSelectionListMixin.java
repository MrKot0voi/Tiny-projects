package com.mrmelon54.DraggableLists.mixin.server;

import com.mrmelon54.DraggableLists.CrashGuard;
import com.mrmelon54.DraggableLists.DragItem;
import com.mrmelon54.DraggableLists.DragList;
import com.mrmelon54.DraggableLists.DragManager;
import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.duck.ServerListDuckProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@OnlyIn(Dist.CLIENT)
@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> implements DragList<ServerData, ServerSelectionList.OnlineServerEntry> {
    @Shadow
    @Final
    private JoinMultiplayerScreen screen;

    @Unique
    private final DragManager<ServerData, ServerSelectionList.OnlineServerEntry> draggable_lists$dragManager = new DragManager<>(this);

    @Shadow
    public abstract void setSelected(ServerSelectionList.Entry entry);

    @Shadow
    public abstract void updateOnlineServers(ServerList servers);

    @Shadow
    public abstract int getRowWidth();

    public ServerSelectionListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @Override
    protected void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.draggable_lists$dragManager.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
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
            && DraggableLists.CONFIG.serverDraggingEnabled != null
            && DraggableLists.CONFIG.serverDraggingEnabled.isEnabled()
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
    public DragItem<ServerData, ServerSelectionList.OnlineServerEntry> draggable_lists$getEntryAtPosition(double mouseX, double mouseY) {
        AbstractSelectionList.Entry<?> entry = this.getEntryAtPosition(mouseX, mouseY);
        if (entry instanceof ServerSelectionList.OnlineServerEntry onlineEntry) {
            return (DragItem<ServerData, ServerSelectionList.OnlineServerEntry>) onlineEntry;
        }
        return null;
    }

    @Override
    public int draggable_lists$getIndexOfEntry(DragItem<ServerData, ServerSelectionList.OnlineServerEntry> item) {
        return item == null ? -1 : this.children().indexOf(item.draggable_lists$getUnderlyingEntry());
    }

    @Override
    public void draggable_lists$setDragging(boolean isDragging) {
        super.setDragging(isDragging);
    }

    @Override
    public int draggable_lists$getHeaderHeight() {
        return 0;
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
    public void draggable_lists$moveEntry(DragItem<ServerData, ServerSelectionList.OnlineServerEntry> item, int index) {
        if (this.screen == null) return;
        ServerList servers = this.screen.getServers();
        if (servers instanceof ServerListDuckProvider duck) {
            duck.draggable_lists$moveItem(item, index);
            servers.save();
            this.updateOnlineServers(servers);
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
        ServerSelectionList.Entry entry = this.children().get(index);
        if (entry == null) {
            return;
        }
        int oldY = entry.getY();
        entry.setY(top);
        try {
            this.renderItem(guiGraphics, mouseX, mouseY, partialTick, entry);
        } catch (Throwable t) {
            CrashGuard.report("ServerSelectionListMixin.renderItem", t);
        } finally {
            entry.setY(oldY);
        }
    }
}
