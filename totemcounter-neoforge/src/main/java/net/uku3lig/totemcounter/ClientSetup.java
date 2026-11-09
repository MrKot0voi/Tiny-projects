package net.uku3lig.totemcounter;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.uku3lig.totemcounter.config.TotemConfigScreen;
import net.uku3lig.totemcounter.config.TotemCounterConfig;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void initialize(IEventBus modEventBus, ModContainer modContainer) {
        TotemCounterConfig.load();
        modEventBus.addListener(ClientEvents::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEvents::registerClientCommands);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                () -> (container, parent) -> new TotemConfigScreen(parent));
    }
}
