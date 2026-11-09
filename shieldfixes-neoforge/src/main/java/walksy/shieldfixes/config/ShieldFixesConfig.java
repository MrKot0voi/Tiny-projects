package walksy.shieldfixes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Настройки мода. Раньше были в WalksyLib (Config.builder), теперь ModConfigSpec.
 * Значения по умолчанию совпадают с Fabric-версией.
 */
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

    public static boolean modEnabled() {
        return MOD_ENABLED.get();
    }

    public static boolean factorDelay() {
        return FACTOR_DELAY.get();
    }
}
