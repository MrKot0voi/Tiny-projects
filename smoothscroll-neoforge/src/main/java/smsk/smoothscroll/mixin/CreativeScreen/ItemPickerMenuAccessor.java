package smsk.smoothscroll.mixin.CreativeScreen;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Доступ к числу рядов креативного меню (protected calculateRowCount).
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public interface ItemPickerMenuAccessor {
    @Invoker("calculateRowCount")
    int invokeCalculateRowCount();
}
