package me.teamaster.nopeeking;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Конфиг мода. Раньше был Cloth Config (AutoConfig) в no-peeking.toml,
 * теперь ModConfigSpec. Значения по умолчанию совпадают с 1.2.4.
 */
public final class NoPeekingConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> COMMANDS_TO_OBFUSCATE;
    private static final ModConfigSpec.ConfigValue<String> OBFUSCATION_CHAR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        COMMANDS_TO_OBFUSCATE = builder
                .comment("List of commands to be obfuscated.")
                .defineList("commandsToObfuscate", Arrays.asList("/l", "/login", "/register"), o -> o instanceof String);

        OBFUSCATION_CHAR = builder
                .comment("The character used in text obfuscation. If you enter multiple characters, only the first one will be used.")
                .define("obfuscationChar", "*");

        SPEC = builder.build();
    }

    private NoPeekingConfig() {
    }

    public static List<? extends String> commandsToObfuscate() {
        return COMMANDS_TO_OBFUSCATE.get();
    }

    public static char obfuscationChar() {
        String value = OBFUSCATION_CHAR.get();
        return value.isEmpty() ? '*' : value.charAt(0);
    }
}
