package nukeduck.armorchroma;

import java.util.Optional;
import net.minecraft.core.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

public class MaterialHelper {
   private static final String TURTLE_MATERIAL = "turtle";

   @Nullable
   public static String getMaterial(Item item) {
      Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
      if (equippable != null) {
         Optional<ResourceKey<EquipmentAsset>> optionalAsset = equippable.assetId();
         if (optionalAsset.isPresent()) {
            ResourceKey<EquipmentAsset> asset = optionalAsset.get();
            if (asset.equals(EquipmentAssets.TURTLE_SCUTE)) {
               return TURTLE_MATERIAL;
            }

            return asset.location().getPath();
         }
      }

      return null;
   }
}
