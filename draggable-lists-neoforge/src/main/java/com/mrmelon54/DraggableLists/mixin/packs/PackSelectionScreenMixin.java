package com.mrmelon54.DraggableLists.mixin.packs;

import com.mrmelon54.DraggableLists.CrashGuard;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PackSelectionScreen.class)
public abstract class PackSelectionScreenMixin extends Screen {
    @Shadow
    private TransferableSelectionList selectedPackList;

    @Shadow
    private TransferableSelectionList availablePackList;

    @Shadow
    @Final
    private PackSelectionModel model;

    @Shadow
    protected abstract void closeWatcher();

    protected PackSelectionScreenMixin(Component title) {
        super(title);
    }

    @Override
    public void onClose() {
        try {
            if (this.selectedPackList != null && this.selectedPackList.isDragging()) {
                this.selectedPackList.mouseReleased(null);
            }
            if (this.availablePackList != null && this.availablePackList.isDragging()) {
                this.availablePackList.mouseReleased(null);
            }
        } catch (Throwable t) {
            CrashGuard.report("PackSelectionScreenMixin.onClose", t);
        }
        if (this.model != null) {
            this.model.commit();
        }
        this.closeWatcher();
    }
}
