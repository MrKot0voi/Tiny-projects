package nukeduck.armorchroma.config;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Objects;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import nukeduck.armorchroma.ArmorChromaRenderLayers;

public class ArmorIcon {
   public static final int ICON_SIZE = 9;
   private static final int SPAN = 28;
   private static final String TEXTURE_PATH = "textures/gui/armor_chroma.png";
   public final Identifier texture;
   public final int u;
   public final int v;
   public final int color;

   public ArmorIcon(int i) {
      this(i, -1);
   }

   public ArmorIcon(int i, int color) {
      this("minecraft", i, color);
   }

   public ArmorIcon(String modid, int i) {
      this(modid, i, -1);
   }

   public ArmorIcon(String modid, int i, int color) {
      this.texture = Identifier.fromNamespaceAndPath(modid, TEXTURE_PATH);
      if (i >= 0) {
         this.u = i % SPAN * ICON_SIZE;
         this.v = i / SPAN * ICON_SIZE;
      } else {
         this.u = 256 + i % SPAN * ICON_SIZE;
         this.v = 256 + ((i + 1) / SPAN - 1) * ICON_SIZE;
      }

      this.color = color;
   }

   public void draw(GuiGraphics context, int x, int y) {
      this.draw(context, RenderPipelines.GUI_TEXTURED, x, y);
   }

   public void draw(GuiGraphics context, int x, int y, int uOffset, int width) {
      this.draw(context, RenderPipelines.GUI_TEXTURED, x + uOffset, y, this.u + uOffset, this.v, width, ICON_SIZE);
   }

   public void drawMasked(GuiGraphics context, int x, int y) {
      this.draw(context, ArmorChromaRenderLayers.getMaskedIcon(), x, y);
   }

   public void drawMasked(GuiGraphics context, int x, int y, int uOffset, int width) {
      this.draw(context, ArmorChromaRenderLayers.getMaskedIcon(), x + uOffset, y, this.u + uOffset, this.v, width, ICON_SIZE);
   }

   private void draw(GuiGraphics context, RenderPipeline pipeline, int x, int y) {
      this.draw(context, pipeline, x, y, this.u, this.v, ICON_SIZE, ICON_SIZE);
   }

   private void draw(GuiGraphics context, RenderPipeline pipeline, int x, int y, int u, int v, int width, int height) {
      context.blit(pipeline, this.texture, x, y, u, v, width, height, 256, 256, this.color);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ArmorIcon armorIcon = (ArmorIcon)o;
         return this.u == armorIcon.u && this.v == armorIcon.v && this.color == armorIcon.color && Objects.equals(this.texture, armorIcon.texture);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.texture, this.u, this.v, this.color);
   }
}
