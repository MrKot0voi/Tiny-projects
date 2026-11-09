package walksy.shieldfixes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import walksy.shieldfixes.config.ShieldFixesConfig;
import walksy.shieldfixes.manager.EnvironmentEffectShieldManager;

@Mod(value = ShieldFixes.MODID, dist = Dist.CLIENT)
public class ShieldFixes {
    public static final String MODID = "shieldfixes";

    private static EnvironmentEffectShieldManager environmentEffectShieldManager;

    public ShieldFixes(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ShieldFixesConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        environmentEffectShieldManager = new EnvironmentEffectShieldManager();
    }

    public static EnvironmentEffectShieldManager getEnvironmentEffectShieldManager() {
        return environmentEffectShieldManager;
    }
}
