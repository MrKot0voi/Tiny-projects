package smsk.smoothscroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// ТЕСТОВЫЙ харнесс (только в ветке runclient-test): без мира открывает чат, подсказки и гоняет прокрутку.
// Включается системным свойством smoothscroll.selftest=true (см. build.gradle, блок runs).
final class SelfTest {
    private static int tick = -1;
    private static int waiting = 0;

    private SelfTest() {
    }

    static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (tick < 0) {
            // Ждём, пока игрок подключится к локальному серверу (нужен для чата с подсказками и креатива).
            if (mc.player == null || mc.level == null) {
                waiting++;
                if (waiting == 40) {
                    try {
                        net.minecraft.client.multiplayer.resolver.ServerAddress address = new net.minecraft.client.multiplayer.resolver.ServerAddress("localhost", 25565);
                        net.minecraft.client.multiplayer.ServerData data = new net.minecraft.client.multiplayer.ServerData("Local", "localhost:25565", net.minecraft.client.multiplayer.ServerData.Type.OTHER);
                        net.minecraft.client.gui.screens.ConnectScreen.startConnecting(mc.screen, mc, address, data, false, null);
                        SmoothSc.LOGGER.info("SELFTEST connecting to localhost:25565");
                    } catch (Throwable t) {
                        SmoothSc.LOGGER.error("SELFTEST connect failed", t);
                    }
                }
                return;
            }
            tick = 0;
            SmoothSc.LOGGER.info("SELFTEST player joined");
        }

        tick++;
        try {
            switch (tick) {
                case 20 -> {
                    for (int i = 0; i < 80; i++) {
                        mc.gui.getChat().addMessage(Component.literal("selftest line " + i));
                    }
                    SmoothSc.LOGGER.info("SELFTEST step 1 chat messages ok");
                }
                case 40 -> {
                    mc.setScreen(new ChatScreen("", false));
                    SmoothSc.LOGGER.info("SELFTEST step 2 chat screen ok");
                }
                case 60 -> {
                    mc.gui.getChat().scrollChat(-5);
                    if (mc.screen != null) {
                        mc.screen.mouseScrolled(10, 10, 0, -3);
                    }

                    SmoothSc.LOGGER.info("SELFTEST step 3 chat and screen scroll ok");
                }
                case 80 -> {
                    mc.setScreen(new ChatScreen("/help", false));
                    SmoothSc.LOGGER.info("SELFTEST step 4 suggestions screen ok");
                }
                case 100 -> {
                    if (mc.screen != null) {
                        mc.screen.mouseScrolled(10, 10, 0, -2);
                        mc.screen.mouseScrolled(10, 10, 0, 2);
                    }

                    SmoothSc.LOGGER.info("SELFTEST step 5 suggestions scroll ok");
                }
                case 120 -> {
                    mc.setScreen(new CreativeModeInventoryScreen(mc.player, FeatureFlags.DEFAULT_FLAGS, true));
                    SmoothSc.LOGGER.info("SELFTEST step 6 creative screen ok");
                }
                case 140 -> {
                    if (mc.screen != null) {
                        mc.screen.mouseScrolled(200, 200, 0, -3);
                    }

                    SmoothSc.LOGGER.info("SELFTEST step 7 creative scroll down ok");
                }
                case 170 -> {
                    if (mc.screen != null) {
                        mc.screen.mouseScrolled(200, 200, 0, 1);
                    }

                    SmoothSc.LOGGER.info("SELFTEST step 8 creative scroll up ok");
                }
                case 200 -> {
                    mc.setScreen(null);
                    mc.gui.getChat().resetChatScroll();
                    SmoothSc.LOGGER.info("SELFTEST DONE");
                }
                default -> {
                }
            }
        } catch (Throwable t) {
            SmoothSc.LOGGER.error("SELFTEST FAIL at tick " + tick, t);
        }
    }
}
