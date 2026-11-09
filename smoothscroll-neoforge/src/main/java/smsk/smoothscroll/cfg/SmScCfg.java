package smsk.smoothscroll.cfg;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Конфиг Smooth Scroll на ModConfigSpec. Ключи, группы и дефолты совпадают с оригиналом 2.6.7.
 * Проценты в конфиге переводятся в коэффициенты (0..1) в геттерах.
 */
public final class SmScCfg {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.DoubleValue HOTBAR_SMOOTHNESS;
    private static final ModConfigSpec.BooleanValue HOTBAR_ROLLOVER;
    private static final ModConfigSpec.BooleanValue HOTBAR_STATIC_SELECTOR;

    private static final ModConfigSpec.DoubleValue CHAT_SMOOTHNESS;
    private static final ModConfigSpec.DoubleValue CHAT_OPENING_SMOOTHNESS;
    private static final ModConfigSpec.DoubleValue CHAT_SCROLLING_SPEED;
    private static final ModConfigSpec.DoubleValue CHAT_SUGGESTION_SMOOTHNESS;
    private static final ModConfigSpec.DoubleValue CHAT_SUGGESTION_SPEED;

    private static final ModConfigSpec.DoubleValue CREATIVE_SMOOTHNESS;

    private static final ModConfigSpec.DoubleValue ENTRY_SMOOTHNESS;
    private static final ModConfigSpec.DoubleValue ENTRY_SPEED;

    private static final ModConfigSpec.DoubleValue TEXT_SMOOTHNESS;
    private static final ModConfigSpec.DoubleValue TEXT_SPEED;
    private static final ModConfigSpec.DoubleValue TEXT_CURSOR_MARGIN;
    private static final ModConfigSpec.BooleanValue TEXT_CUSTOM_CURSOR_UPDATE;

    private static final ModConfigSpec.BooleanValue MISC_MASK_DEBUG;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Smoothness values are in %. Scrolling speed values are in pixels.",
                "0 % means animation off (no smoothness) and bigger values mean slower animation speed (high smoothness).")
                .push("hotbar");
        HOTBAR_SMOOTHNESS = b.defineInRange("smoothness", 20.0, 0.0, 100.0);
        HOTBAR_ROLLOVER = b.define("rollover", true);
        HOTBAR_STATIC_SELECTOR = b.comment("Keep the selector fixed and move the hotbar items instead.")
                .define("staticSelector", false);
        b.pop();

        b.push("chat");
        CHAT_SMOOTHNESS = b.defineInRange("smoothness", 50.0, 0.0, 100.0);
        CHAT_OPENING_SMOOTHNESS = b.defineInRange("openingSmoothness", 50.0, 0.0, 100.0);
        CHAT_SCROLLING_SPEED = b.comment("0 means automatic speed.").defineInRange("scrollingSpeed", 0.0, 0.0, 100.0);
        CHAT_SUGGESTION_SMOOTHNESS = b.defineInRange("suggestionSmoothness", 50.0, 0.0, 100.0);
        CHAT_SUGGESTION_SPEED = b.comment("0 means automatic speed.").defineInRange("suggestionSpeed", 30.0, 0.0, 100.0);
        b.pop();

        b.push("creativeScreen");
        CREATIVE_SMOOTHNESS = b.defineInRange("smoothness", 50.0, 0.0, 100.0);
        b.pop();

        b.push("entryList");
        ENTRY_SMOOTHNESS = b.defineInRange("smoothness", 50.0, 0.0, 100.0);
        ENTRY_SPEED = b.comment("0 means automatic speed.").defineInRange("speed", 30.0, 0.0, 100.0);
        b.pop();

        b.push("textInputField");
        TEXT_SMOOTHNESS = b.defineInRange("smoothness", 50.0, 0.0, 100.0);
        TEXT_SPEED = b.comment("0 means automatic speed.").defineInRange("speed", 100.0, 0.0, 300.0);
        TEXT_CURSOR_MARGIN = b.defineInRange("cursorMargin", 10.0, 0.0, 100.0);
        TEXT_CUSTOM_CURSOR_UPDATE = b.comment("Use the custom cursor-follow logic instead of the vanilla one.")
                .define("customCursorUpdate", true);
        b.pop();

        b.push("misc");
        MISC_MASK_DEBUG = b.define("enableMaskDebug", false);
        b.pop();

        SPEC = b.build();
    }

    private SmScCfg() {
    }

    public static float hotbarSmoothness() { return (float) (HOTBAR_SMOOTHNESS.get() / 100.0); }
    public static boolean hotbarRollover() { return HOTBAR_ROLLOVER.get(); }
    public static boolean staticSelector() { return HOTBAR_STATIC_SELECTOR.get(); }

    public static float chatSmoothness() { return (float) (CHAT_SMOOTHNESS.get() / 100.0); }
    public static float chatOpeningSmoothness() { return (float) (CHAT_OPENING_SMOOTHNESS.get() / 100.0); }
    public static float chatAmount() { return CHAT_SCROLLING_SPEED.get().floatValue(); }
    public static float suggestionWindowSmoothness() { return (float) (CHAT_SUGGESTION_SMOOTHNESS.get() / 100.0); }
    public static float suggestionWindowAmount() { return CHAT_SUGGESTION_SPEED.get().floatValue(); }

    public static float creativeScreenSmoothness() { return (float) (CREATIVE_SMOOTHNESS.get() / 100.0); }

    public static float entryListSmoothness() { return (float) (ENTRY_SMOOTHNESS.get() / 100.0); }
    public static double entryListAmount() { return ENTRY_SPEED.get(); }

    public static float textSmoothness() { return (float) (TEXT_SMOOTHNESS.get() / 100.0); }
    public static float textAmount() { return TEXT_SPEED.get().floatValue(); }
    public static float textMargin() { return TEXT_CURSOR_MARGIN.get().floatValue(); }
    public static boolean textCustomUpdate() { return TEXT_CUSTOM_CURSOR_UPDATE.get(); }

    public static boolean enableMaskDebug() { return MISC_MASK_DEBUG.get(); }
}
