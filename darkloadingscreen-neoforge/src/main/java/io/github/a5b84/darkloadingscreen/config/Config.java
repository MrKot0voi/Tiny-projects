package io.github.a5b84.darkloadingscreen.config;

import io.github.a5b84.darkloadingscreen.CrashGuard;
import io.github.a5b84.darkloadingscreen.DarkLoadingScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final float FADE_DURATION_FACTOR = DarkLoadingScreen.VANILLA_FADE_OUT_DURATION / 2;
    public static final float MAX_FADE_DURATION = 5;

    public final int bg, bar, barBg, border, logo;
    public final float bgR, bgG, bgB;
    public final float logoR, logoG, logoB;

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

    public Config(
            int bg, int bar, int barBg, int border, int logo,
            float fadeIn, float fadeOut
    ) {
        this.bg = bg;
        this.bar = bar;
        this.barBg = barBg;
        this.border = border;
        this.logo = logo;
        this.fadeIn = Math.clamp(fadeIn, 0.0f, MAX_FADE_DURATION);
        this.fadeOut = Math.clamp(fadeOut, 0.0f, MAX_FADE_DURATION);

        bgR = getChannel(bg, 16);
        bgG = getChannel(bg, 8);
        bgB = getChannel(bg, 0);
        logoR = getChannel(logo, 16);
        logoG = getChannel(logo, 8);
        logoB = getChannel(logo, 0);

        fadeInMs = this.fadeIn * FADE_DURATION_FACTOR;
        fadeOutMs = this.fadeOut * FADE_DURATION_FACTOR;
    }

    private static float getChannel(int color, int offset) {
        return ((color >> offset) & 0xff) / 255f;
    }

    private static <T> T getSafe(ModConfigSpec.ConfigValue<T> val) {
        try {
            return SPEC.isLoaded() ? val.get() : val.getDefault();
        } catch (Throwable t) {
            return val.getDefault();
        }
    }

    public static Config read() {
        try {
            if (!SPEC.isLoaded()) {
                return DEFAULT;
            }
            return new Config(
                    parseColor(getSafe(BACKGROUND), DEFAULT.bg, "background"),
                    parseColor(getSafe(BAR), DEFAULT.bar, "bar"),
                    parseColor(getSafe(BAR_BACKGROUND), DEFAULT.barBg, "barBackground"),
                    parseColor(getSafe(BORDER), DEFAULT.border, "border"),
                    parseColor(getSafe(LOGO), DEFAULT.logo, "logo"),
                    getSafe(FADE_IN).floatValue(),
                    getSafe(FADE_OUT).floatValue()
            );
        } catch (Throwable t) {
            CrashGuard.report("Config.read", t);
            return DEFAULT;
        }
    }

    private static int parseColor(String value, int fallback, String key) {
        if (value == null) {
            return fallback;
        }
        String clean = value.trim().startsWith("#") ? value.trim().substring(1) : value.trim();
        try {
            return Integer.parseInt(clean, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            LOGGER.warn("[Dark Loading Screen] Invalid color '{}' for option '{}'", value, key);
            CrashGuard.warn("Config.parseColor", "Некорректный HEX-цвет '" + value + "' для '" + key + "', применён стандартный");
            return fallback;
        }
    }

    private static boolean isHexColor(Object value) {
        return value instanceof String s && s.matches("#?[0-9a-fA-F]{6}");
    }
}
