package nukeduck.armorchroma.config;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import nukeduck.armorchroma.MaterialHelper;

public class IconTable {
   public final Map<String, Integer> materials = new HashMap<>();
   public final Map<String, Integer> items = new HashMap<>();
   public final Map<String, Integer> special = new HashMap<>();

   public void putAll(IconTable other) {
      this.materials.putAll(other.materials);
      this.items.putAll(other.items);
      this.special.putAll(other.special);
   }

   public Integer getIconIndex(ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         Integer i = null;
         Item item = stack.getItem();
         String material = MaterialHelper.getMaterial(item);
         if (material != null) {
            i = Util.getGlob(this.materials, material);
         }

         if (i == null) {
            i = Util.getGlob(this.items, BuiltInRegistries.ITEM.getKey(item).getPath());
         }

         return i;
      }
   }

   public Integer getSpecialIndex(String key) {
      return this.special.get(key);
   }
}
