package com.ic3dwtf.pingnametag;

import com.ic3dwtf.pingnametag.config.PingNametagConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PingNametag.MODID, dist = Dist.CLIENT)
public class PingNametag {
   public static final String MODID = "nametag_ping";

   public PingNametag(ModContainer container) {
      container.registerConfig(ModConfig.Type.CLIENT, PingNametagConfig.SPEC);
      container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
   }
}
