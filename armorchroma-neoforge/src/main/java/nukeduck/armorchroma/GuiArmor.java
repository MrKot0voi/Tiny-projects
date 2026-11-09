package nukeduck.armorchroma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import nukeduck.armorchroma.config.ArmorIcon;
import nukeduck.armorchroma.config.SpecialIconKey;
import nukeduck.armorchroma.config.Util;

public class GuiArmor {
   private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("armorchroma", "textures/gui/background.png");
   private static final int LEFT_HALF_U_OFFSET = 0;
   private static final int RIGHT_HALF_U_OFFSET = 4;
   private static final int HALF_ICON_WIDTH = 5;
   private static final int[] BG_COLORS = new int[]{-12924161, -12851878, -256, -25344, -1232384, -9359167};
   private static final int ROW_SPACING = 5;
   private static final int ARMOR_PER_ROW = 20;
   private static final AttributeSupplier FALLBACK_ATTRIBUTES = AttributeSupplier.builder().add(Attributes.ARMOR).build();
   private final Minecraft client = Minecraft.getInstance();

   public void draw(GuiGraphics context, int x, int y) {
      List<ArmorBarSegment> segments = new ArrayList<>(EquipmentSlot.VALUES.size());
      int totalPoints = this.buildArmorSegments(this.client.player, segments);
      if (totalPoints > 0) {
         int barPoints = 0;
         int compressedRows = ArmorChroma.config.compressBar() ? this.compressRows(segments, totalPoints) : 0;
         this.drawBackground(context, x, y, compressedRows);

         for (ArmorBarSegment segment : segments) {
            this.drawSegment(context, x, y, barPoints, segment);
            barPoints += segment.getArmorPoints();
         }
      }
   }

   private void drawBackground(GuiGraphics context, int x, int y, int level) {
      boolean drawBorder = level > 0;
      if (ArmorChroma.config.renderBackground() || drawBorder) {
         context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0.0F, 0.0F, 81, 9, 256, 256);
         if (drawBorder) {
            int color = level <= BG_COLORS.length ? BG_COLORS[level - 1] : BG_COLORS[BG_COLORS.length - 1];
            context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x - 1, y - 1, 81.0F, 0.0F, 83, 11, 256, 256, color);
         }
      }
   }

   private void drawSegment(GuiGraphics context, int x, int y, int barPoints, ArmorBarSegment segment) {
      y -= barPoints / ARMOR_PER_ROW * ROW_SPACING;
      int stackPoints = segment.getArmorPoints();

      int space;
      while ((space = ARMOR_PER_ROW - barPoints % ARMOR_PER_ROW) <= stackPoints) {
         this.drawPartialRow(context, x, y, ARMOR_PER_ROW - space, space, segment);
         this.addZOffset(context, -3);
         y -= ROW_SPACING;
         barPoints += space;
         stackPoints -= space;
      }

      if (stackPoints > 0) {
         this.drawPartialRow(context, x, y, ARMOR_PER_ROW - space, stackPoints, segment);
         this.addZOffset(context, -1);
      }
   }

   private void drawPartialRow(GuiGraphics context, int left, int top, int barPoints, int stackPoints, ArmorBarSegment segment) {
      ArmorIcon icon = segment.getIcon();
      if (segment.hasGlint()) {
         this.addZOffset(context, 2);
      }

      int i = barPoints & 1;
      int x = left + barPoints * 4;
      if (i == 1) {
         this.drawMaskedIcon(context, x - 4, top, icon, segment.getLeadingMask(), 4);
         x += 4;
      }

      while (i < stackPoints - 1) {
         icon.draw(context, x, top);
         i += 2;
         x += 8;
      }

      if (i < stackPoints) {
         this.drawMaskedIcon(context, x, top, icon, segment.getTrailingMask(), 0);
      }

      if (segment.hasGlint()) {
         this.drawTexturedGlintRect(context, left + barPoints * 4, top, left, top, stackPoints * 4 + 1, 9);
         this.addZOffset(context, -2);
      }
   }

   private int buildArmorSegments(LocalPlayer player, List<ArmorBarSegment> segments) {
      AttributeMap attributes = new AttributeMap(FALLBACK_ATTRIBUTES);
      AttributeInstance armor = attributes.getInstance(Attributes.ARMOR);
      if (armor == null) {
         return 0;
      } else {
         int displayedArmorCap = ArmorChroma.config.getDisplayedArmorCap();
         int totalPoints = (int)((EntityAttributeInstanceAccess)armor).armorChroma_getUnclampedValue();

         for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = player.getItemBySlot(slot);
            stack.forEachModifier(slot, (attribute, modifier) -> {
               if (attribute == Attributes.ARMOR && !armor.hasModifier(modifier.id())) {
                  armor.addTransientModifier(modifier);
               }
            });
            int nextTotalPoints = Math.min(displayedArmorCap, (int)((EntityAttributeInstanceAccess)armor).armorChroma_getUnclampedValue());
            int pointDelta = nextTotalPoints - totalPoints;
            totalPoints = nextTotalPoints;
            if (pointDelta > 0) {
               ArmorIcon icon = ArmorChroma.ICON_DATA.getIcon(stack);
               boolean hasGlint = ArmorChroma.config.renderGlint() && stack.hasFoil();
               String modId = Util.getModId(stack);
               ArmorIcon leadingMask = ArmorChroma.ICON_DATA.getSpecial(modId, SpecialIconKey.LEADING_MASK);
               ArmorIcon trailingMask = ArmorChroma.ICON_DATA.getSpecial(modId, SpecialIconKey.TRAILING_MASK);
               ArmorBarSegment segment = new ArmorBarSegment(pointDelta, icon, leadingMask, trailingMask, hasGlint);
               if (!segments.isEmpty() && segment.canMergeWith(segments.getLast())) {
                  segments.getLast().addArmorPoints(segment.getArmorPoints());
               } else {
                  segments.add(segment);
               }
            }
         }

         if (ArmorChroma.config.reverse()) {
            Collections.reverse(segments);
         }

         return totalPoints;
      }
   }

   private int compressRows(List<ArmorBarSegment> segments, int totalPoints) {
      int compressedRows = (totalPoints - 1) / ARMOR_PER_ROW;
      int compressedPoints = compressedRows * ARMOR_PER_ROW;
      int segmentsToRemove = 0;
      int pointsSoFar = 0;

      for (ArmorBarSegment segment : segments) {
         pointsSoFar += segment.getArmorPoints();
         if (pointsSoFar > compressedPoints) {
            segment.setArmorPoints(pointsSoFar - compressedPoints);
            break;
         }

         segmentsToRemove++;
      }

      segments.subList(0, segmentsToRemove).clear();
      return compressedRows;
   }

   private void drawMaskedIcon(GuiGraphics context, int x, int y, ArmorIcon icon, ArmorIcon mask, int uOffset) {
      mask.draw(context, x, y, uOffset, 5);
      icon.drawMasked(context, x, y, uOffset, 5);
   }

   private void drawTexturedGlintRect(GuiGraphics context, int x, int y, float u, float v, int width, int height) {
      float intensity = this.client.options.glintStrength().get().floatValue() * ArmorChroma.config.glintIntensity();
      float clamped = Mth.clamp(intensity, 0.0F, 1.0F);
      int color = ARGB.colorFromFloat(1.0F, clamped, clamped, clamped);
      context.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/misc/enchanted_item_glint.png"), x, y, u, v, width, height, 256, 256, color);
   }

   private void addZOffset(GuiGraphics context, int z) {
   }
}
