package walksy.shieldfixes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ShieldFixesConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue MOD_ENABLED;
    private static final ModConfigSpec.BooleanValue FACTOR_DELAY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        MOD_ENABLED = builder
                .comment("Enable Shield Fixes.")
                .define("modEnabled", true);

        FACTOR_DELAY = builder
                .comment("Only count a shield as up after vanilla's short raise delay.")
                .define("factorDelay", false);

        SPEC = builder.build();
    }

    private ShieldFixesConfig() {
    }

    private static <T> T getSafe(ModConfigSpec.ConfigValue<T> val) {
        try {
            return SPEC.isLoaded() ? val.get() : val.getDefault();
        } catch (Throwable t) {
            return val.getDefault();
        }
    }

    public static boolean modEnabled() {
        return getSafe(MOD_ENABLED);
    }

    public static boolean factorDelay() {
        return getSafe(FACTOR_DELAY);
    }
}
