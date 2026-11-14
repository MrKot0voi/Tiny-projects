package smsk.smoothscroll.mixin.CreativeScreen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Доступ к позиции экрана (leftPos/topPos объявлены в AbstractContainerScreen).
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int smoothscroll$getLeftPos();

    @Accessor("topPos")
    int smoothscroll$getTopPos();
}
