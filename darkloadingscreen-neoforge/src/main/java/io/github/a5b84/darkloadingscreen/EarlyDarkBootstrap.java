package io.github.a5b84.darkloadingscreen;

import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ранний экран NeoForge (fml-loadingscreen) выбирает тёмную тему по флагу fml.earlyWindowDarkMode.
 * Этот бутстрап запускается до открытия окна (ImmediateWindowHandler.load) и включает флаг,
 * поэтому стартовый экран тоже становится тёмным, а не только LoadingOverlay.
 */
public final class EarlyDarkBootstrap implements GraphicsBootstrapper {
    private static final Logger LOG = LogManager.getLogger("DarkLoadingScreen");

    @Override
    public String name() {
        return "dark_loading_screen";
    }

    @Override
    public void bootstrap(String[] args) {
        System.setProperty("fml.earlyWindowDarkMode", "true");
        LOG.info("Early loading screen: dark mode forced (fml.earlyWindowDarkMode=true)");
    }
}
