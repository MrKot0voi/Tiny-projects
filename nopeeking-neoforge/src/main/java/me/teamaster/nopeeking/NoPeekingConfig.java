package me.teamaster.nopeeking;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Locale;

/**
 * Конфиг No Peeking. Список команд, аргументы которых скрываются в поле чата.
 * Команды можно писать с / и без, регистр не важен. Русские и английские алиасы уже включены.
 */
public final class NoPeekingConfig {
    /** Дефолтный список: английские команды плагинов авторизации и русские алиасы. */
    public static final List<String> DEFAULT_COMMANDS = List.of(
            // английские
            "/l", "/log", "/login", "/logon",
            "/reg", "/r", "/register", "/regist",
            "/changepassword", "/changepass", "/changepw", "/cp",
            "/unregister", "/unreg", "/passwd", "/password", "/pass",
            "/2fa", "/totp",
            // русские
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

    /** true, если первое слово команды (например "/login" или "login") есть в списке. */
    public static boolean isObfuscatedCommand(String word) {
        String typed = normalize(word);
        for (String entry : OBFUSCATED_COMMANDS.get()) {
            if (normalize(entry).equals(typed)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String command) {
        String value = command.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("/") ? value : "/" + value;
    }

    public static char obfuscationChar() {
        String value = OBFUSCATION_CHAR.get();
        return value.isEmpty() ? '*' : value.charAt(0);
    }
}
