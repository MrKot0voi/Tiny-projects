package ru.berdinskiybear.armorhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.berdinskiybear.armorhud.CrashGuard;

public class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private ArmorHudConfig config = new ArmorHudConfig();

    public ConfigManager(String modId) {
        this.file = FMLPaths.CONFIGDIR.get().resolve(modId + ".json");
    }

    public ArmorHudConfig getConfig() {
        if (config == null) {
            config = new ArmorHudConfig();
        }
        sanitize(config);
        return config;
    }

    private static void sanitize(ArmorHudConfig c) {
        if (c == null) return;
        if (c.getAnchor() == null) {
            c.setAnchor(ArmorHudConfig.Anchor.HOTBAR);
            CrashGuard.warn("ConfigManager.sanitize", "Поле anchor было null, сброшено в HOTBAR");
        }
        if (c.getSide() == null) {
            c.setSide(ArmorHudConfig.Side.LEFT);
            CrashGuard.warn("ConfigManager.sanitize", "Поле side было null, сброшено в LEFT");
        }
        if (c.getStyle() == null) {
            c.setStyle(ArmorHudConfig.Style.HOTBAR);
            CrashGuard.warn("ConfigManager.sanitize", "Поле style было null, сброшено в HOTBAR");
        }
        if (c.getOrientation() == null) {
            c.setOrientation(ArmorHudConfig.Orientation.HORIZONTAL);
            CrashGuard.warn("ConfigManager.sanitize", "Поле orientation было null, сброшено в HORIZONTAL");
        }
        if (c.getWidgetShown() == null) {
            c.setWidgetShown(ArmorHudConfig.WidgetShown.NOT_EMPTY);
            CrashGuard.warn("ConfigManager.sanitize", "Поле widgetShown было null, сброшено в NOT_EMPTY");
        }
        if (c.getOffhandSlotBehavior() == null) {
            c.setOffhandSlotBehavior(ArmorHudConfig.OffhandSlotBehavior.ADHERE);
            CrashGuard.warn("ConfigManager.sanitize", "Поле offhandSlotBehavior было null, сброшено в ADHERE");
        }
        if (c.getDurabilityDisplay() == null) {
            c.setDurabilityDisplay(ArmorHudConfig.DurabilityDisplay.BAR);
            CrashGuard.warn("ConfigManager.sanitize", "Поле durabilityDisplay было null, сброшено в BAR");
        }
        if (c.getWarningBobIntensity() < 0) {
            c.setWarningBobIntensity(0);
            CrashGuard.warn("ConfigManager.sanitize", "warningBobIntensity < 0, исправлено на 0");
        }
    }

    public void load() {
        if (!Files.exists(file)) {
            save();
            return;
        }

        try {
            ArmorHudConfig loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), ArmorHudConfig.class);
            config = loaded != null ? loaded : new ArmorHudConfig();
            sanitize(config);
        } catch (Exception e) {
            LOGGER.error("[Armor HUD] Не удалось прочитать {}, используются значения по умолчанию", file, e);
            config = new ArmorHudConfig();
            CrashGuard.report("ConfigManager.load", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[Armor HUD] Не удалось сохранить {}", file, e);
            CrashGuard.report("ConfigManager.save", e);
        }
    }
}
