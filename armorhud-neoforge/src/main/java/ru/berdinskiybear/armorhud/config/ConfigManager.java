package ru.berdinskiybear.armorhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Конфиг Armor HUD в файле config/<modId>.json.
 * Раньше был ukulib ConfigManager с экраном настроек; ukulib под NeoForge 1.21.11 не выпущен,
 * поэтому экрана нет: параметры меняются в файле, переключение виджета — клавишей.
 */
public class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private ArmorHudConfig config = new ArmorHudConfig();

    public ConfigManager(String modId) {
        this.file = FMLPaths.CONFIGDIR.get().resolve(modId + ".json");
    }

    public ArmorHudConfig getConfig() {
        return config;
    }

    public void load() {
        if (!Files.exists(file)) {
            save();
            return;
        }

        try {
            ArmorHudConfig loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), ArmorHudConfig.class);
            config = loaded != null ? loaded : new ArmorHudConfig();
        } catch (Exception e) {
            LOGGER.error("[Armor HUD] Не удалось прочитать {}, используются значения по умолчанию", file, e);
            config = new ArmorHudConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[Armor HUD] Не удалось сохранить {}", file, e);
        }
    }
}
