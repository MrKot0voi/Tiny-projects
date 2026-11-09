package smsk.smoothscroll;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smsk.smoothscroll.cfg.SmScCfg;

@Mod(value = SmoothSc.MODID, dist = Dist.CLIENT)
public class SmoothSc {
    public static final String MODID = "smoothscroll";
    public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");

    // Сколько раз хотбар "прокрутился" через край (для плавного переполнения).
    public static int hotbarRollover = 0;

    public SmoothSc(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SmScCfg.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /** Длительность последнего кадра в тиках (1 тик = 50 мс). */
    public static float getLastFrameDuration() {
        return Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
    }
}
