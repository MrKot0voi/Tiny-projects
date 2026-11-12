package nukeduck.armorchroma;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import nukeduck.armorchroma.config.ArmorIcon;

/**
 * Готовит половинки брони заранее: пиксели иконки там, где непрозрачна маска, остальное прозрачно.
 * Так половинка рисуется обычным GUI_TEXTURED без блендинга и без теста глубины (раньше иконка терялась,
 * оставалась только белая маска).
 */
public final class MaskedIconCache {
   private static final Map<String, Identifier> CACHE = new HashMap<>();

   private MaskedIconCache() {
   }

   public static Identifier get(ArmorIcon icon, ArmorIcon mask) {
      String key = icon.texture.getNamespace() + "_" + icon.u + "_" + icon.v + "_"
            + mask.texture.getNamespace() + "_" + mask.u + "_" + mask.v;
      Identifier cached = CACHE.get(key);
      if (cached != null) {
         return cached;
      }

      Identifier id = Identifier.fromNamespaceAndPath("armorchroma", "masked/" + key);
      try {
         NativeImage iconImage = load(icon.texture);
         NativeImage maskImage = load(mask.texture);
         int size = ArmorIcon.ICON_SIZE;
         NativeImage out = new NativeImage(NativeImage.Format.RGBA, size, size, false);
         for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
               int maskAlpha = (maskImage.getPixel(mask.u + x, mask.v + y) >>> 24) & 0xFF;
               if (maskAlpha != 0) {
                  out.setPixel(x, y, iconImage.getPixel(icon.u + x, icon.v + y));
               } else {
                  out.setPixel(x, y, 0);
               }
            }
         }

         iconImage.close();
         maskImage.close();
         Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> id.toString(), out));
      } catch (IOException | RuntimeException e) {
         ArmorChroma.LOGGER.error("Не удалось собрать половинку брони {}", key, e);
         return icon.texture;
      }

      CACHE.put(key, id);
      return id;
   }

   private static NativeImage load(Identifier texture) throws IOException {
      try (InputStream in = Minecraft.getInstance().getResourceManager().getResource(texture).orElseThrow().open()) {
         return NativeImage.read(in);
      }
   }
}
