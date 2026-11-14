package net.natural.motionblur.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.natural.motionblur.recording.RecordingShaderManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Конфиг мода в naturalmotionblur.json. Экрана настроек на YACL здесь нет:
 * YACL под NeoForge 1.21.11 не выпущен, поэтому настройки меняются командами /mb и клавишей.
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<String> errorMessages = new ArrayList<>();
    private static boolean configReset = false;

    private static ConfigEntries config;

    public static ConfigEntries getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    // Config Logic
    public static void loadConfig() {
        Path configFile = getConfigFile();
        errorMessages.clear();

        config = new ConfigEntries();

        if (!Files.exists(configFile)) {
            saveConfig();
            return;
        }

        try {
            JsonObject json = GSON.fromJson(Files.readString(configFile, StandardCharsets.UTF_8), JsonObject.class);
            if (json == null) { saveConfig(); return; }
            boolean modified = false;

            if (json.has("enabled")) {
                try { config.enabled = parseBool(json.get("enabled").getAsString()); }
                catch (Exception e) { config.enabled = true; errorMessages.add("Toggle option of \"Natural Motion Blur\" was invalid and has been reset to default (enabled)."); modified = true; }
            }
            if (json.has("refreshRateScaling")) {
                try { config.refreshRateScaling = parseBool(json.get("refreshRateScaling").getAsString()); }
                catch (Exception e) { config.refreshRateScaling = true; errorMessages.add("Refresh Rate Scaling option of \"Natural Motion Blur\" was invalid and has been reset to default (enabled)."); modified = true; }
            }
            if (json.has("motionBlurStrength")) {
                try { float v = json.get("motionBlurStrength").getAsFloat(); if (v < 0.0F || v > 2.0F) throw new IllegalArgumentException(); config.motionBlurStrength = v; }
                catch (Exception e) { config.motionBlurStrength = 1.0F; errorMessages.add("Motion Blur Strength option of \"Natural Motion Blur\" was invalid and has been reset to default (1.0)."); modified = true; }
            }
            if (json.has("blurAlgorithm")) {
                try { config.blurAlgorithm = ConfigEntries.BlurAlgorithm.valueOf(json.get("blurAlgorithm").getAsString().toUpperCase()); }
                catch (Exception e) { config.blurAlgorithm = ConfigEntries.BlurAlgorithm.VELOCITY_BASED; errorMessages.add("Blur Algorithm option of \"Natural Motion Blur\" was invalid and has been reset to default (Velocity Based)."); modified = true; }
            }
            if (json.has("blurProfile")) {
                try { config.blurProfile = ConfigEntries.BlurProfile.valueOf(json.get("blurProfile").getAsString().toUpperCase()); }
                catch (Exception e) { config.blurProfile = ConfigEntries.BlurProfile.UNIFORM; errorMessages.add("Blur Profile option of \"Natural Motion Blur\" was invalid and has been reset to default (Uniform)."); modified = true; }
            }
            if (json.has("recordingOverlayTargetFPS")) {
                try {
                    int hz = json.get("recordingOverlayTargetFPS").getAsInt();
                    if (hz < 24 || hz > 240) throw new IllegalArgumentException();
                    config.recordingOverlayTargetFPS = hz;
                } catch (Exception e) { config.recordingOverlayTargetFPS = 60; errorMessages.add("OBS Spout Output FPS Target option of \"Natural Motion Blur\" was invalid and has been reset to default (60)."); modified = true; }
            }
            // OBS Spout Output is runtime-only and must always start disabled on launch.
            config.recordingOverlayEnabled = false;
            if (modified) { saveConfig(); configReset = true; }

        } catch (Exception e) {
            config = new ConfigEntries();
            saveConfig();
            configReset = true;
            errorMessages.add("Config file of mod \"Natural Motion Blur\" could not be loaded correctly and has been reset to default.");
        }
    }

    private static boolean parseBool(String s) {
        if (!"true".equalsIgnoreCase(s) && !"false".equalsIgnoreCase(s)) throw new IllegalArgumentException();
        return Boolean.parseBoolean(s);
    }

    public static void saveConfig() {
        Path configFile = getConfigFile();
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            net.natural.motionblur.CrashGuard.report("ConfigManager.saveConfig", e);
        }
    }

    private static Path getConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve("naturalmotionblur.json");
    }

    public static boolean isConfigReset() {
        return configReset;
    }

    public static List<String> getAndClearErrorMessages() {
        List<String> messagesToReturn = new ArrayList<>(errorMessages);
        errorMessages.clear();

        return messagesToReturn;
    }
}
