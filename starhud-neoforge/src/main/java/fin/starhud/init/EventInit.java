package fin.starhud.init;

import fin.starhud.CrashGuard;
import fin.starhud.Main;
import fin.starhud.config.GeneralSettings;
import fin.starhud.config.Settings;
import fin.starhud.helper.AttackTracker;
import fin.starhud.hud.HUDComponent;
import fin.starhud.screen.EditHUDScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class EventInit {
    private static final GeneralSettings.InGameHUDSettings SETTINGS = Main.settings.generalSettings.inGameSettings;

    public static void init(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(EventInit::onAttack);
        NeoForge.EVENT_BUS.addListener(EventInit::onAttackEndTick);
        NeoForge.EVENT_BUS.addListener(EventInit::onOpenEditHUDKeyPressed);
        NeoForge.EVENT_BUS.addListener(EventInit::onToggleHUDKeyPressed);
        NeoForge.EVENT_BUS.addListener(EventInit::onClientStopping);
        NeoForge.EVENT_BUS.addListener(EventInit::onHUDRender);
        eventBus.addListener(EventInit::onLogin);
    }

    public static void onClientStopping(GameShuttingDownEvent event) {
        AutoConfig.getConfigHolder(Settings.class).save();
    }

    public static void onOpenEditHUDKeyPressed(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (Main.openEditHUDKey.consumeClick()) {
            client.setScreenAndShow(new EditHUDScreen(Component.nullToEmpty("Edit HUD"), client.screen));
        }
    }

    public static void onToggleHUDKeyPressed(ClientTickEvent.Post event) {
        try {
            while (Main.toggleHUDKey.consumeClick()) {
                Main.settings.generalSettings.inGameSettings.disableHUDRendering = !Main.settings.generalSettings.inGameSettings.disableHUDRendering;
            }
            while (Main.toggleInventoryHUDKey.consumeClick()) {
                Main.settings.inventorySettings.base.shouldRender = !Main.settings.inventorySettings.base.shouldRender;
                HUDComponent.getInstance().lastCollect = 0L;
            }
        } catch (Throwable t) {
            CrashGuard.report(t, "EventInit.onToggleHUDKeyPressed");
        }
    }

    public static void onHUDRender(RenderGuiEvent.Post event) {
        try {
            if (SETTINGS.disableHUDRendering) return;
            if (Minecraft.getInstance().options.hideGui) return;
            if (Minecraft.getInstance().screen instanceof EditHUDScreen) return;

            HUDComponent.getInstance().collectAll();
            HUDComponent.getInstance().renderAll(event.getGuiGraphics());
        } catch (Throwable t) {
            CrashGuard.report(t, "EventInit.onHUDRender");
        }
    }

    public static void onAttack(AttackEntityEvent event) {
        AttackTracker.onAttack(event.getEntity(), event.getEntity().level(), event.getTarget());
    }

    public static void onAttackEndTick(ClientTickEvent.Post event) {
        AttackTracker.onEndTick(Minecraft.getInstance());
    }

    public static void onLogin(FMLLoadCompleteEvent event) {
        HUDComponent.getInstance().init();
    }
}
