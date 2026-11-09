package com.ic3dwtf.pingnametag.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Конфиг мода. Раньше был Gson-файлом и Cloth Config под Fabric,
 * теперь ModConfigSpec. Значения по умолчанию совпадают с оригиналом.
 */
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

   public static PingNametagConfig get() {
      return INSTANCE;
   }

   public boolean enabled() {
      return ENABLED.get();
   }

   public String textFormat() {
      return TEXT_FORMAT.get();
   }

   public boolean showOwnPing() {
      return SHOW_OWN_PING.get();
   }

   /**
    * Пороги выстраиваются по возрастанию: каждый следующий не меньше предыдущего + 1,
    * как и в исходном моде.
    */
   public int colorForPing(int ping) {
      int good = GOOD_PING_MAX.get();
      int medium = Math.max(good + 1, MEDIUM_PING_MAX.get());
      int bad = Math.max(medium + 1, BAD_PING_MAX.get());

      if (ping <= good) {
         return GOOD_COLOR.get();
      }
      if (ping <= medium) {
         return MEDIUM_COLOR.get();
      }
      if (ping <= bad) {
         return BAD_COLOR.get();
      }
      return WORST_COLOR.get();
   }
}
