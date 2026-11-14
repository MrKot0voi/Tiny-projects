package net.natural.motionblur;

import net.natural.motionblur.config.CommandManager;
import net.natural.motionblur.config.ConfigManager;
import net.natural.motionblur.config.EventManager;
import net.natural.motionblur.config.KeybindingManager;
import net.natural.motionblur.recording.RecordingShaderManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = NaturalMotionBlurMod.ID, dist = Dist.CLIENT)
public class NaturalMotionBlurMod {
    public static final String ID = "naturalmotionblur";

    public NaturalMotionBlurMod(IEventBus modBus) {
        ConfigManager.loadConfig();

        modBus.addListener(RegisterKeyMappingsEvent.class, KeybindingManager::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, KeybindingManager::onClientTickPre);
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, CommandManager::onRegisterClientCommands);
        EventManager.registerEvents();

        Runtime.getRuntime().addShutdownHook(new Thread(RecordingShaderManager::destroy,
                "nmb-recording-output-cleanup"));
    }
}
