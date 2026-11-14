package nukeduck.armorchroma.config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import nukeduck.armorchroma.ArmorChroma;
import nukeduck.armorchroma.CrashGuard;

public class IconData implements ResourceManagerReloadListener {
   private final Map<String, IconTable> mods = new HashMap<>();
   private static final ArmorIcon FALLBACK_ICON = new ArmorIcon(0);
   private static final String DEFAULT = "default";
   private static final String MINECRAFT = "minecraft";
   private static final String ICONS_PATH = "textures/gui/armor_chroma.json";

   public ArmorIcon getIcon(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return FALLBACK_ICON;
      }
      String modId = Util.getModId(stack);
      if (modId == null) {
         return this.getSpecial(SpecialIconKey.DEFAULT);
      }
      IconTable mod = this.mods.get(modId);
      if (mod != null) {
         Integer i = mod.getIconIndex(stack);
         return i != null ? new ArmorIcon(modId, i, Util.getColor(stack)) : this.getSpecial(modId, SpecialIconKey.DEFAULT);
      } else {
         return this.getSpecial(SpecialIconKey.DEFAULT);
      }
   }

   public ArmorIcon getSpecial(SpecialIconKey key) {
      return this.getSpecial(MINECRAFT, key);
   }

   public ArmorIcon getSpecial(String modid, SpecialIconKey key) {
      if (modid == null || key == null) {
         return FALLBACK_ICON;
      }
      ArmorIcon icon = this.getSpecialWithoutFallback(modid, key);
      if (icon == null && !modid.equals(MINECRAFT)) {
         icon = this.getSpecialWithoutFallback(MINECRAFT, key);
      }

      return icon != null ? icon : FALLBACK_ICON;
   }

   private ArmorIcon getSpecialWithoutFallback(String modid, SpecialIconKey key) {
      IconTable mod = this.mods.get(modid);
      if (mod != null) {
         Integer i = mod.getSpecialIndex(key.value);
         if (i != null) {
            return new ArmorIcon(modid, i);
         }
      }

      return null;
   }

   private void ensureFallbackMinecraftTable() {
      IconTable minecraft = this.mods.computeIfAbsent(MINECRAFT, k -> new IconTable());
      minecraft.special.putIfAbsent(DEFAULT, 0);
      minecraft.special.putIfAbsent(SpecialIconKey.LEADING_MASK.value, 1);
      minecraft.special.putIfAbsent(SpecialIconKey.TRAILING_MASK.value, 2);
   }

   @Override
   public void onResourceManagerReload(ResourceManager manager) {
      this.mods.clear();
      if (manager == null) {
         ensureFallbackMinecraftTable();
         CrashGuard.warn("IconData.onResourceManagerReload", "ResourceManager == null, применены резервные иконки");
         return;
      }

      try {
         for (IModInfo modInfo : ModList.get().getMods()) {
            String modid = modInfo.getModId();
            List<Resource> resources = manager.getResourceStack(Identifier.fromNamespaceAndPath(modid, ICONS_PATH));
            if (!resources.isEmpty()) {
               for (Resource resource : resources) {
                  try (Reader reader = new BufferedReader(new InputStreamReader(resource.open()))) {
                     IconTable mod = new Gson().fromJson(reader, IconTable.class);
                     if (mod != null) {
                        this.mods.merge(modid, mod, (a, b) -> {
                           a.putAll(b);
                           return a;
                        });
                     }
                  } catch (JsonIOException | IOException | JsonSyntaxException e) {
                     ArmorChroma.LOGGER.error("[Armor Chroma] Error loading icons for {}", modid, e);
                     CrashGuard.report("IconData.load(" + modid + ")", e);
                  }
               }
            }
         }
      } catch (Throwable t) {
         CrashGuard.report("IconData.onResourceManagerReload", t);
      }

      IconTable minecraft = this.mods.get(MINECRAFT);
      if (minecraft != null
         && minecraft.getSpecialIndex(DEFAULT) != null
         && minecraft.getSpecialIndex(SpecialIconKey.LEADING_MASK.value) != null
         && minecraft.getSpecialIndex(SpecialIconKey.TRAILING_MASK.value) != null) {
         ArmorChroma.LOGGER.info("[Armor Chroma] Loaded icons for {} mods", Math.max(0, this.mods.size() - 1));
      } else {
         ensureFallbackMinecraftTable();
         CrashGuard.warn("IconData.onResourceManagerReload", "Не найдены базовые иконки armor_chroma.json, включены встроенные резервные иконки");
      }
   }
}
