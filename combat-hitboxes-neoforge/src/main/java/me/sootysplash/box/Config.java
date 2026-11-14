package me.sootysplash.box;

import java.awt.Color;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
   public static final ModConfigSpec SPEC;

   private static final ModConfigSpec.BooleanValue ENABLED;
   private static final ModConfigSpec.BooleanValue HIDE_ARROW;
   private static final ModConfigSpec.BooleanValue HIDE_FIREWORKS;
   private static final ModConfigSpec.BooleanValue HIDE_ITEMS;
   private static final ModConfigSpec.BooleanValue CHANGE_TARGET_COLOR;
   private static final ModConfigSpec.IntValue EYE_COLOR;
   private static final ModConfigSpec.IntValue LOOK_COLOR;
   private static final ModConfigSpec.IntValue HITBOX_COLOR;
   private static final ModConfigSpec.IntValue TARGET_BOX_COLOR;
   private static final ModConfigSpec.BooleanValue HITBOX_HURT;
   private static final ModConfigSpec.BooleanValue RENDER_EYE_HEIGHT;
   private static final ModConfigSpec.BooleanValue RENDER_LOOK_DIR;
   private static final ModConfigSpec.BooleanValue LINE_LOOK_DIR;
   private static final ModConfigSpec.IntValue HURT_BOX_COLOR;
   private static final ModConfigSpec.DoubleValue LINE1;
   private static final ModConfigSpec.DoubleValue DIST_FOR_2;
   private static final ModConfigSpec.DoubleValue LINE2;
   private static final ModConfigSpec.BooleanValue OUTLINE_ENABLED;
   private static final ModConfigSpec.IntValue OUTLINE_COLOR;
   private static final ModConfigSpec.DoubleValue OUTLINE_MULTIPLIER;

   private static final Config INSTANCE = new Config();

   static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

      builder.push("general");
      ENABLED = builder.comment("Replace vanilla hitbox rendering with Combat Hitboxes.").define("enabled", true);
      HIDE_ARROW = builder.comment("Hide arrows stuck in the player model.").define("hideArrow", false);
      HIDE_FIREWORKS = builder.comment("Do not draw hitboxes of firework rockets.").define("hideFireworks", false);
      HIDE_ITEMS = builder.comment("Do not draw hitboxes of dropped items.").define("hideItems", false);
      CHANGE_TARGET_COLOR = builder.comment("Use the target color for the entity you are looking at.").define("changeTargetColor", true);
      builder.pop();

      builder.push("colors");
      EYE_COLOR = builder.comment("Eye height line color (ARGB).").defineInRange("eyeColor", Color.RED.getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE);
      LOOK_COLOR = builder.comment("Look direction color (ARGB).").defineInRange("lookColor", Color.BLUE.getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE);
      HITBOX_COLOR = builder.comment("Hitbox color (ARGB).").defineInRange("hitBoxColor", Color.WHITE.getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE);
      TARGET_BOX_COLOR = builder.comment("Hitbox color of the targeted entity (ARGB).").defineInRange("targetBoxColor", Color.RED.getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE);
      HURT_BOX_COLOR = builder.comment("Hitbox color of an entity that was recently hurt (ARGB).").defineInRange("hurtBoxColor", Color.MAGENTA.getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE);
      OUTLINE_COLOR = builder.comment("Outline color (ARGB).").defineInRange("outlineColor", Color.BLACK.getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE);
      builder.pop();

      builder.push("display");
      HITBOX_HURT = builder.comment("Use the hurt color for recently damaged entities.").define("hitBoxHurt", false);
      RENDER_EYE_HEIGHT = builder.comment("Draw the eye height line.").define("renderEyeHeight", true);
      RENDER_LOOK_DIR = builder.comment("Draw the look direction.").define("renderLookDir", true);
      LINE_LOOK_DIR = builder.comment("Draw the look direction as a line instead of an arrow.").define("lineLookDir", true);
      LINE1 = builder.comment("Line width when the entity is closer than displayDistance.").defineInRange("line1", 2.5, 0.0, 64.0);
      DIST_FOR_2 = builder.comment("Distance in blocks where line2 starts to be used.").defineInRange("distFor2", 32.0, 0.0, 1024.0);
      LINE2 = builder.comment("Line width when the entity is farther than distFor2.").defineInRange("line2", 2.5, 0.0, 64.0);
      OUTLINE_ENABLED = builder.comment("Draw an outline around the hitbox.").define("outlineEnabled", false);
      OUTLINE_MULTIPLIER = builder.comment("Outline width relative to the line width.").defineInRange("outlineMultiplier", 2.0, 0.0, 16.0);
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

   public static Config getInstance() {
      return INSTANCE;
   }

   public boolean enabled() {
      return getSafe(ENABLED);
   }

   public boolean hideArrow() {
      return getSafe(HIDE_ARROW);
   }

   public boolean hideFireworks() {
      return getSafe(HIDE_FIREWORKS);
   }

   public boolean hideItems() {
      return getSafe(HIDE_ITEMS);
   }

   public boolean changeTargetColor() {
      return getSafe(CHANGE_TARGET_COLOR);
   }

   public int eyeColor() {
      return getSafe(EYE_COLOR);
   }

   public int lookColor() {
      return getSafe(LOOK_COLOR);
   }

   public int hitBoxColor() {
      return getSafe(HITBOX_COLOR);
   }

   public int targetBoxColor() {
      return getSafe(TARGET_BOX_COLOR);
   }

   public int hurtBoxColor() {
      return getSafe(HURT_BOX_COLOR);
   }

   public int outlineColor() {
      return getSafe(OUTLINE_COLOR);
   }

   public boolean hitBoxHurt() {
      return getSafe(HITBOX_HURT);
   }

   public boolean renderEyeHeight() {
      return getSafe(RENDER_EYE_HEIGHT);
   }

   public boolean renderLookDir() {
      return getSafe(RENDER_LOOK_DIR);
   }

   public boolean lineLookDir() {
      return getSafe(LINE_LOOK_DIR);
   }

   public float line1() {
      return getSafe(LINE1).floatValue();
   }

   public double distFor2() {
      return getSafe(DIST_FOR_2);
   }

   public float line2() {
      return getSafe(LINE2).floatValue();
   }

   public boolean outlineEnabled() {
      return getSafe(OUTLINE_ENABLED);
   }

   public float outlineMultiplier() {
      return getSafe(OUTLINE_MULTIPLIER).floatValue();
   }
}
