package io.github.a5b84.darkloadingscreen.config;

import io.github.a5b84.darkloadingscreen.DarkLoadingScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Настройки мода. Раньше были в JSON через Cloth Config, теперь ModConfigSpec.
 * Цвета хранятся как строки RRGGBB (как в Cloth Config).
 */
public class Config {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Factor to convert UI time (seconds) to ms */
    // (Dividing by 2 because the game waits twice as long)
    public static final float FADE_DURATION_FACTOR = DarkLoadingScreen.VANILLA_FADE_OUT_DURATION / 2;
    /** Maximum fade duration (just in case) */
    public static final float MAX_FADE_DURATION = 5;

    // Colors
    public final int bg, bar, barBg, border, logo;
    // RGB channels of some colors
    public final float bgR, bgG, bgB;
    public final float logoR, logoG, logoB;

    // Fade durations
    public final float fadeIn, fadeOut;
    public final float fadeInMs, fadeOutMs;

    public static final Config DEFAULT = new Config(
            0x14181c, 0xe22837, 0x14181c, 0x303336, 0xffffff,
            DarkLoadingScreen.VANILLA_FADE_IN_DURATION / FADE_DURATION_FACTOR,
            DarkLoadingScreen.VANILLA_FADE_OUT_DURATION / FADE_DURATION_FACTOR
    );

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<String> BACKGROUND;
    private static final ModConfigSpec.ConfigValue<String> BAR;
    private static final ModConfigSpec.ConfigValue<String> BAR_BACKGROUND;
    private static final ModConfigSpec.ConfigValue<String> BORDER;
    private static final ModConfigSpec.ConfigValue<String> LOGO;
    private static final ModConfigSpec.DoubleValue FADE_IN;
    private static final ModConfigSpec.DoubleValue FADE_OUT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("colors");
        BACKGROUND = builder.comment("Background color, RRGGBB hex.").define("background", String.format("%06x", DEFAULT.bg), Config::isHexColor);
        BAR = builder.comment("Progress bar color, RRGGBB hex.").define("bar", String.format("%06x", DEFAULT.bar), Config::isHexColor);
        BAR_BACKGROUND = builder.comment("Progress bar background color, RRGGBB hex.").define("barBackground", String.format("%06x", DEFAULT.barBg), Config::isHexColor);
        BORDER = builder.comment("Progress bar border color, RRGGBB hex.").define("border", String.format("%06x", DEFAULT.border), Config::isHexColor);
        LOGO = builder.comment("Mojang logo color, RRGGBB hex.").define("logo", String.format("%06x", DEFAULT.logo), Config::isHexColor);
        builder.pop();

        builder.push("fade");
        FADE_IN = builder.comment("Fade in time, seconds.").defineInRange("fadeIn", DEFAULT.fadeIn, 0.0, MAX_FADE_DURATION);
        FADE_OUT = builder.comment("Fade out time, seconds.").defineInRange("fadeOut", DEFAULT.fadeOut, 0.0, MAX_FADE_DURATION);
        builder.pop();

        SPEC = builder.build();
    }

    /**
     * @param fadeIn Fade in time in seconds
     * @param fadeOut Fade out time in seconds
     */
    public Config(
            int bg, int bar, int barBg, int border, int logo,
            float fadeIn, float fadeOut
    ) {
        this.bg = bg;
        this.bar = bar;
        this.barBg = barBg;
        this.border = border;
        this.logo = logo;
        this.fadeIn = Math.min(fadeIn, MAX_FADE_DURATION);
        this.fadeOut = Math.min(fadeOut, MAX_FADE_DURATION);

        // Splitting some colors in floats
        bgR = getChannel(bg, 16);
        bgG = getChannel(bg, 8);
        bgB = getChannel(bg, 0);
        logoR = getChannel(logo, 16);
        logoG = getChannel(logo, 8);
        logoB = getChannel(logo, 0);

        // Calculate durations
        fadeInMs = this.fadeIn * FADE_DURATION_FACTOR;
        fadeOutMs = this.fadeOut * FADE_DURATION_FACTOR;
    }

    /**
     * @param offset Channel offset in bits
     * @return the corresponding channel value between {@code 0} and {@code 1}
     */
    private static float getChannel(int color, int offset) {
        return ((color >> offset) & 0xff) / 255f;
    }

    /** Reads the current values of the config */
    public static Config read() {
        return new Config(
                parseColor(BACKGROUND.get(), DEFAULT.bg, "background"),
                parseColor(BAR.get(), DEFAULT.bar, "bar"),
                parseColor(BAR_BACKGROUND.get(), DEFAULT.barBg, "barBackground"),
                parseColor(BORDER.get(), DEFAULT.border, "border"),
                parseColor(LOGO.get(), DEFAULT.logo, "logo"),
                FADE_IN.get().floatValue(),
                FADE_OUT.get().floatValue()
        );
    }

    private static int parseColor(String value, int fallback, String key) {
        try {
            return Integer.parseInt(value, 16);
        } catch (NumberFormatException e) {
            LOGGER.warn("[Dark Loading Screen] Invalid color '{}' for option '{}'", value, key);
            return fallback;
        }
    }

    private static boolean isHexColor(Object value) {
        return value instanceof String s && s.matches("[0-9a-fA-F]{6}");
    }
}
