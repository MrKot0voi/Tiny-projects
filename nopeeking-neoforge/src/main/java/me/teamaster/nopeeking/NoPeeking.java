package me.teamaster.nopeeking;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = NoPeeking.MODID, dist = Dist.CLIENT)
public class NoPeeking {
    public static final String MODID = "no_peeking";

    public NoPeeking(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, NoPeekingConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
