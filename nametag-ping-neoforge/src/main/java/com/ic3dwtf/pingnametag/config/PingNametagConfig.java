package com.ic3dwtf.pingnametag.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PingNametagConfig {
   public static final ModConfigSpec SPEC;

   private static final ModConfigSpec.BooleanValue ENABLED;
   private static final ModConfigSpec.ConfigValue<String> TEXT_FORMAT;
   private static final ModConfigSpec.BooleanValue SHOW_OWN_PING;
   private static final ModConfigSpec.IntValue GOOD_PING_MAX;
   private static final ModConfigSpec.IntValue MEDIUM_PING_MAX;
   private static final ModConfigSpec.IntValue BAD_PING_MAX;
   private static final ModConfigSpec.IntValue GOOD_COLOR;
   private static final ModConfigSpec.IntValue MEDIUM_COLOR;
   private static final ModConfigSpec.IntValue BAD_COLOR;
   private static final ModConfigSpec.IntValue WORST_COLOR;

   private static final PingNametagConfig INSTANCE = new PingNametagConfig();

   static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

      builder.push("general");
      ENABLED = builder.comment("Enable the ping overlay.").define("enabled", true);
      TEXT_FORMAT = builder.comment("Text appended to the nametag. Use %ping% for the ping value.").define("textFormat", " (%ping%ms)");
      SHOW_OWN_PING = builder.comment("Show your own ping on your nametag.").define("showOwnPing", true);
      GOOD_PING_MAX = builder.comment("Ping up to this value is shown in the good color.").defineInRange("goodPingMax", 80, 1, 2000);
      MEDIUM_PING_MAX = builder.comment("Ping up to this value is shown in the medium color.").defineInRange("mediumPingMax", 150, 1, 2000);
      BAD_PING_MAX = builder.comment("Ping up to this value is shown in the bad color.").defineInRange("badPingMax", 250, 1, 2000);
      GOOD_COLOR = builder.comment("Color for good ping (RGB).").defineInRange("goodColor", 0x55FF55, 0, 0xFFFFFF);
      MEDIUM_COLOR = builder.comment("Color for medium ping (RGB).").defineInRange("mediumColor", 0xFFFF55, 0, 0xFFFFFF);
      BAD_COLOR = builder.comment("Color for bad ping (RGB).").defineInRange("badColor", 0xFFAA00, 0, 0xFFFFFF);
      WORST_COLOR = builder.comment("Color for the worst ping (RGB).").defineInRange("worstColor", 0xFF5555, 0, 0xFFFFFF);
      builder.pop();

      SPEC = builder.build();
   }

   private PingNametagConfig() {
   }

   private static <T> T getSafe(ModConfigSpec.ConfigValue<T> val) {
      try {
         return SPEC.isLoaded() ? val.get() : val.getDefault();
      } catch (Throwable t) {
         return val.getDefault();
      }
   }

   public static PingNametagConfig get() {
      return INSTANCE;
   }

   public boolean enabled() {
      return getSafe(ENABLED);
   }

   public String textFormat() {
      String fmt = getSafe(TEXT_FORMAT);
      return fmt != null ? fmt : " (%ping%ms)";
   }

   public boolean showOwnPing() {
      return getSafe(SHOW_OWN_PING);
   }

   public int colorForPing(int ping) {
      int good = getSafe(GOOD_PING_MAX);
      int medium = Math.max(good + 1, getSafe(MEDIUM_PING_MAX));
      int bad = Math.max(medium + 1, getSafe(BAD_PING_MAX));

      if (ping <= good) {
         return getSafe(GOOD_COLOR);
      }
      if (ping <= medium) {
         return getSafe(MEDIUM_COLOR);
      }
      if (ping <= bad) {
         return getSafe(BAD_COLOR);
      }
      return getSafe(WORST_COLOR);
   }
}
