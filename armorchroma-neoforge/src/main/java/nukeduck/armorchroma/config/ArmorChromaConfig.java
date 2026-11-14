package nukeduck.armorchroma.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ArmorChromaConfig {
   public static final ModConfigSpec SPEC;
   private static final ModConfigSpec.BooleanValue ENABLED;
   private static final ModConfigSpec.BooleanValue RENDER_GLINT;
   private static final ModConfigSpec.DoubleValue GLINT_INTENSITY;
   private static final ModConfigSpec.BooleanValue RENDER_BACKGROUND;
   private static final ModConfigSpec.BooleanValue COMPRESS_BAR;
   private static final ModConfigSpec.IntValue DISPLAYED_ARMOR_CAP;
   private static final ModConfigSpec.BooleanValue REVERSE;
   private static final ModConfigSpec.BooleanValue SHOW_MATERIAL_IN_TOOLTIP;

   static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
      builder.push("general");
      ENABLED = builder.comment("Replace the vanilla armor bar with the Armor Chroma bar.").define("enabled", true);
      RENDER_GLINT = builder.comment("Show the enchantment sheen on enchanted armor.").define("renderGlint", true);
      GLINT_INTENSITY = builder.comment("Strength of the enchantment sheen, multiplied by the game's glint setting.")
         .defineInRange("glintIntensity", 1.0, 0.0, 1.0);
      RENDER_BACKGROUND = builder.comment("Always draw the armor bar background.").define("renderBackground", true);
      COMPRESS_BAR = builder.comment("Compress armor above 20 points into overlapping rows.").define("compressBar", false);
      DISPLAYED_ARMOR_CAP = builder.comment("Maximum armor points shown on the bar.")
         .defineInRange("displayedArmorCap", 100, 0, Integer.MAX_VALUE);
      REVERSE = builder.comment("Reverse the order of armor segments on the bar.").define("reverse", false);
      SHOW_MATERIAL_IN_TOOLTIP = builder.comment("Show the armor material in item tooltips.").define("showMaterialInTooltip", false);
      builder.pop();
      SPEC = builder.build();
   }

   private static <T> T getSafe(ModConfigSpec.ConfigValue<T> val) {
      try {
         return SPEC.isLoaded() ? val.get() : val.getDefault();
      } catch (Throwable t) {
         return val.getDefault();
      }
   }

   public boolean isEnabled() {
      return getSafe(ENABLED);
   }

   public boolean renderGlint() {
      return getSafe(RENDER_GLINT);
   }

   public float glintIntensity() {
      return getSafe(GLINT_INTENSITY).floatValue();
   }

   public boolean renderBackground() {
      return getSafe(RENDER_BACKGROUND);
   }

   public boolean compressBar() {
      return getSafe(COMPRESS_BAR);
   }

   public int getDisplayedArmorCap() {
      return getSafe(DISPLAYED_ARMOR_CAP);
   }

   public boolean reverse() {
      return getSafe(REVERSE);
   }

   public boolean showMaterialInTooltip() {
      return getSafe(SHOW_MATERIAL_IN_TOOLTIP);
   }
}
