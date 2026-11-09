package nukeduck.armorchroma;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import nukeduck.armorchroma.config.ArmorChromaConfig;
import nukeduck.armorchroma.config.IconData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ArmorChroma.MODID, dist = Dist.CLIENT)
public class ArmorChroma {
   public static final String MODID = "armorchroma";
   public static final GuiArmor GUI = new GuiArmor();
   public static final Logger LOGGER = LogManager.getLogger();
   public static final int TEXTURE_SIZE = 256;
   public static final ArmorChromaConfig config = new ArmorChromaConfig();
   public static final IconData ICON_DATA = new IconData();

   public ArmorChroma(IEventBus modBus, ModContainer container) {
      container.registerConfig(ModConfig.Type.CLIENT, ArmorChromaConfig.SPEC);
      container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

      modBus.addListener(this::onAddClientReloadListeners);
      modBus.addListener(this::onRegisterRenderPipelines);

      if (!FMLEnvironment.isProduction()) {
         NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
      }
   }

   private void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath(MODID, "icon_data"), ICON_DATA);
   }

   private void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
      ArmorChromaRenderLayers.registerPipelines(event);
   }

   private void onRegisterCommands(RegisterCommandsEvent event) {
      ArmorChromaDebugCommand.register(event.getDispatcher());
   }
}
