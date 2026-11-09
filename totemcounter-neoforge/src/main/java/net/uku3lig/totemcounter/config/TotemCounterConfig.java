package net.uku3lig.totemcounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import net.uku3lig.totemcounter.TotemCounter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class TotemCounterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TotemCounterConfig INSTANCE = new TotemCounterConfig();

    // Totem display
    private boolean displayEnabled = true;
    private int x = -1;
    private int y = -1;
    private boolean useDefaultTotem = false;
    private boolean displayColors = true;
    private boolean coloredXpBar = false;
    private boolean alwaysShowBar = false;
    private boolean showPopCounter = false;

    // Pop counter
    private boolean counterEnabled = true;
    private boolean separator = true;
    private boolean counterColors = true;
    private boolean showInTab = false;

    public static TotemCounterConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path file = getFile();
        if (!Files.exists(file)) {
            INSTANCE.save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            TotemCounterConfig loaded = GSON.fromJson(reader, TotemCounterConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (Exception exception) {
            TotemCounter.LOGGER.error("Could not read TotemCounter config at {}", file, exception);
        }
    }

    public void save() {
        Path file = getFile();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            TotemCounter.LOGGER.error("Could not save TotemCounter config at {}", file, exception);
        }
    }

    private static Path getFile() {
        return FMLPaths.CONFIGDIR.get().resolve("totemcounter.json");
    }

    public boolean isDisplayEnabled() { return displayEnabled; }
    public void setDisplayEnabled(boolean value) { displayEnabled = value; }
    public int getX() { return x; }
    public void setX(int value) { x = value; }
    public int getY() { return y; }
    public void setY(int value) { y = value; }
    public boolean isUseDefaultTotem() { return useDefaultTotem; }
    public void setUseDefaultTotem(boolean value) { useDefaultTotem = value; }
    public boolean isDisplayColors() { return displayColors; }
    public void setDisplayColors(boolean value) { displayColors = value; }
    public boolean isColoredXpBar() { return coloredXpBar; }
    public void setColoredXpBar(boolean value) { coloredXpBar = value; }
    public boolean isAlwaysShowBar() { return alwaysShowBar; }
    public void setAlwaysShowBar(boolean value) { alwaysShowBar = value; }
    public boolean isShowPopCounter() { return showPopCounter; }
    public void setShowPopCounter(boolean value) { showPopCounter = value; }
    public boolean isCounterEnabled() { return counterEnabled; }
    public void setCounterEnabled(boolean value) { counterEnabled = value; }
    public boolean isSeparator() { return separator; }
    public void setSeparator(boolean value) { separator = value; }
    public boolean isCounterColors() { return counterColors; }
    public void setCounterColors(boolean value) { counterColors = value; }
    public boolean isShowInTab() { return showInTab; }
    public void setShowInTab(boolean value) { showInTab = value; }
}
