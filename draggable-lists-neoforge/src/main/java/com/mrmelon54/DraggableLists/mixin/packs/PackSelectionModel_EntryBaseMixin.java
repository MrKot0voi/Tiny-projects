package com.mrmelon54.DraggableLists.mixin.packs;

import com.mrmelon54.DraggableLists.duck.AbstractPackDuckProvider;
import com.mrmelon54.DraggableLists.duck.ResourcePackOrganizerDuckProvider;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PackSelectionModel.EntryBase.class)
public abstract class PackSelectionModel_EntryBaseMixin implements AbstractPackDuckProvider {
    @Shadow
    @Final
    private Pack pack;

    @Shadow
    @Final
    @Dynamic("this$0 is outer PackSelectionModel reference")
    private PackSelectionModel this$0;

    @Shadow
    protected abstract List<Pack> getSelfList();

    @Override
    public void draggable_lists$moveTo(int index) {
        if (this.pack.isFixedPosition()) {
            return;
        }
        List<Pack> list = this.getSelfList();
        list.remove(this.pack);
        int clampedIndex = Math.max(0, Math.min(index, list.size()));
        list.add(clampedIndex, this.pack);
        List<Pack> fixedPacks = list.stream().filter(Pack::isFixedPosition).toList();
        list.removeAll(fixedPacks);
        list.addAll(fixedPacks);
        ((ResourcePackOrganizerDuckProvider) this.this$0).draggable_lists$updateSelectedList((PackSelectionModel.EntryBase) (Object) this);
    }
}
