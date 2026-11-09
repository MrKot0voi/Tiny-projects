package me.sootysplash.box;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = Main.MODID, dist = Dist.CLIENT)
public class Main {
   public static final String MODID = "combat_hitboxes";
   public static Minecraft mc = Minecraft.getInstance();
   public static final Logger LOGGER = LogManager.getLogger("CombatHitboxes");

   public Main(ModContainer container) {
      container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
      container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
      LOGGER.info("CombatHitboxes | Sootysplash was here!");
   }
}
