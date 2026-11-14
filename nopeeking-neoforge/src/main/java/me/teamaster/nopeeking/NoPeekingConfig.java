package me.teamaster.nopeeking;

import java.util.List;
import java.util.Locale;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class NoPeekingConfig {
    public static final List<String> DEFAULT_COMMANDS = List.of(
            "/l", "/log", "/login", "/logon",
            "/reg", "/r", "/register", "/regist",
            "/changepassword", "/changepass", "/changepw", "/cp",
            "/unregister", "/unreg", "/passwd", "/password", "/pass",
            "/2fa", "/totp",
            "/логин", "/логон", "/войти", "/вход",
            "/рег", "/регистрация", "/регистрироваться", "/зарегистрироваться",
            "/смена", "/сменапароля", "/сменитьпароль", "/пароль"
    );

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> OBFUSCATED_COMMANDS;
    private static final ModConfigSpec.ConfigValue<String> OBFUSCATION_CHAR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        OBFUSCATED_COMMANDS = builder
                .comment("Commands whose arguments are hidden in the chat box while you type them.",
                        "Write them with or without the leading slash, letter case does not matter.",
                        "Add your server's own commands here, one entry per command.")
                .defineList("obfuscatedCommands", DEFAULT_COMMANDS, o -> o instanceof String);

        OBFUSCATION_CHAR = builder
                .comment("The character used in text obfuscation. If you enter multiple characters, only the first one will be used.")
                .define("obfuscationChar", "*");

        SPEC = builder.build();
    }

    private NoPeekingConfig() {
    }

    private static <T> T getSafe(ModConfigSpec.ConfigValue<T> val) {
        try {
            return SPEC.isLoaded() ? val.get() : val.getDefault();
        } catch (Throwable t) {
            return val.getDefault();
        }
    }

    public static boolean isObfuscatedCommand(String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        String typed = normalize(word);
        List<? extends String> list = getSafe(OBFUSCATED_COMMANDS);
        if (list == null) {
            list = DEFAULT_COMMANDS;
        }
        for (String entry : list) {
            if (entry != null && !entry.isBlank() && normalize(entry).equals(typed)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String command) {
        if (command == null) {
            return "/";
        }
        String value = command.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("/") ? value : "/" + value;
    }

    public static char obfuscationChar() {
        String value = getSafe(OBFUSCATION_CHAR);
        return (value == null || value.isEmpty()) ? '*' : value.charAt(0);
    }
}
