package nukeduck.armorchroma;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import nukeduck.armorchroma.config.ArmorIcon;

/**
 * Свечение зачарования, обрезанное по форме иконки (или половинки).
 * Пиксель свечения виден только там, где непрозрачна иконка (и маска для половинок).
 */
public final class GlintCellCache {
   private static final Identifier GLINT = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
   private static final Map<String, Identifier> CACHE = new HashMap<>();

   private GlintCellCache() {
   }

   public static Identifier full(ArmorIcon icon) {
      return build(icon, null);
   }

   public static Identifier half(ArmorIcon icon, ArmorIcon mask) {
      return build(icon, mask);
   }

   private static Identifier build(ArmorIcon icon, ArmorIcon mask) {
      String key = icon.texture.getNamespace() + "_" + icon.u + "_" + icon.v + "_"
            + (mask == null ? "full" : mask.texture.getNamespace() + "_" + mask.u + "_" + mask.v);
      Identifier cached = CACHE.get(key);
      if (cached != null) {
         return cached;
      }

      Identifier id = Identifier.fromNamespaceAndPath("armorchroma", "glint/" + key);
      try {
         NativeImage iconImage = MaskedIconCache.load(icon.texture);
         NativeImage maskImage = mask == null ? null : MaskedIconCache.load(mask.texture);
         NativeImage glint = MaskedIconCache.load(GLINT);
         int size = ArmorIcon.ICON_SIZE;
         int glintW = glint.getWidth();
         int glintH = glint.getHeight();
         NativeImage out = new NativeImage(NativeImage.Format.RGBA, size, size, false);

         for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
               int iconAlpha = alpha(iconImage.getPixel(icon.u + x, icon.v + y));
               boolean inMask = maskImage == null || alpha(maskImage.getPixel(mask.u + x, mask.v + y)) != 0;
               if (iconAlpha == 0 || !inMask) {
                  out.setPixel(x, y, 0);
                  continue;
               }
               int g = glint.getPixel((icon.u + x) % glintW, (icon.v + y) % glintH);
               int a = alpha(g) * iconAlpha / 255;
               out.setPixel(x, y, (a << 24) | (g & 0x00FFFFFF));
            }
         }

         iconImage.close();
         if (maskImage != null) {
            maskImage.close();
         }
         glint.close();
         Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> id.toString(), out));
      } catch (Exception e) {
         ArmorChroma.LOGGER.error("Не удалось собрать свечение для {}", key, e);
         return null;
      }

      CACHE.put(key, id);
      return id;
   }

   private static int alpha(int argb) {
      return (argb >>> 24) & 0xFF;
   }
}
