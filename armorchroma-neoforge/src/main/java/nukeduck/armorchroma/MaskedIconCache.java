package nukeduck.armorchroma;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import nukeduck.armorchroma.config.ArmorIcon;

public final class MaskedIconCache {
   private static final Map<String, Identifier> CACHE = new HashMap<>();

   private MaskedIconCache() {
   }

   public static Identifier get(ArmorIcon icon, ArmorIcon mask) {
      if (icon == null) {
         return Identifier.withDefaultNamespace("textures/gui/armor_chroma.png");
      }
      if (mask == null) {
         return icon.texture;
      }
      String key = icon.texture.getNamespace() + "_" + icon.u + "_" + icon.v + "_"
            + mask.texture.getNamespace() + "_" + mask.u + "_" + mask.v;
      Identifier cached = CACHE.get(key);
      if (cached != null) {
         return cached;
      }

      Identifier id = Identifier.fromNamespaceAndPath("armorchroma", "masked/" + key);
      try (NativeImage iconImage = load(icon.texture);
           NativeImage maskImage = load(mask.texture)) {
         if (iconImage == null || maskImage == null) {
            return icon.texture;
         }
         int size = ArmorIcon.ICON_SIZE;
         NativeImage out = new NativeImage(NativeImage.Format.RGBA, size, size, false);
         int iconW = iconImage.getWidth();
         int iconH = iconImage.getHeight();
         int maskW = maskImage.getWidth();
         int maskH = maskImage.getHeight();

         for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
               int mx = mask.u + x;
               int my = mask.v + y;
               int ix = icon.u + x;
               int iy = icon.v + y;
               int maskAlpha = (mx >= 0 && mx < maskW && my >= 0 && my < maskH)
                     ? ((maskImage.getPixel(mx, my) >>> 24) & 0xFF)
                     : 0;
               if (maskAlpha != 0 && ix >= 0 && ix < iconW && iy >= 0 && iy < iconH) {
                  out.setPixel(x, y, iconImage.getPixel(ix, iy));
               } else {
                  out.setPixel(x, y, 0);
               }
            }
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.getTextureManager() != null) {
            mc.getTextureManager().register(id, new DynamicTexture(() -> id.toString(), out));
            CACHE.put(key, id);
            return id;
         } else {
            out.close();
            return icon.texture;
         }
      } catch (Throwable e) {
         CrashGuard.report("MaskedIconCache.get(" + key + ")", e);
         return icon.texture;
      }
   }

   private static NativeImage load(Identifier texture) throws IOException {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.getResourceManager() == null) {
         return null;
      }
      Optional<Resource> res = mc.getResourceManager().getResource(texture);
      if (res.isEmpty()) {
         CrashGuard.warn("MaskedIconCache.load", "Не найдена текстура " + texture);
         return null;
      }
      try (InputStream in = res.get().open()) {
         return NativeImage.read(in);
      }
   }
}
