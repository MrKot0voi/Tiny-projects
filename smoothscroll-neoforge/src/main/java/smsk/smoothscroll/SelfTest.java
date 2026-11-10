package smsk.smoothscroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// ТЕСТОВЫЙ харнесс (только в ветке runclient-test): без мира открывает чат, подсказки и гоняет прокрутку.
// Включается системным свойством smoothscroll.selftest=true (см. build.gradle, блок runs).
final class SelfTest {
    private static int tick = 0;

    private SelfTest() {
    }

    static void onTick(ClientTickEvent.Post event) {
        tick++;
        Minecraft mc = Minecraft.getInstance();
        try {
            switch (tick) {
                case 100 -> {
                    for (int i = 0; i < 80; i++) {
                        mc.gui.getChat().addMessage(Component.literal("selftest line " + i));
                    }
                    SmoothSc.LOGGER.info("SELFTEST step 1 chat messages ok");
                }
                case 120 -> {
                    mc.setScreen(new ChatScreen("", false));
                    SmoothSc.LOGGER.info("SELFTEST step 2 chat screen ok");
                }
                case 140 -> {
                    mc.gui.getChat().scrollChat(-5);
                    SmoothSc.LOGGER.info("SELFTEST step 3 chat scroll ok");
                }
                case 160 -> {
                    if (mc.screen != null) {
                        mc.screen.mouseScrolled(10, 10, 0, -3);
                    }

                    SmoothSc.LOGGER.info("SELFTEST step 4 screen scroll ok");
                }
                case 200 -> {
                    mc.setScreen(new ChatScreen("/help", false));
                    SmoothSc.LOGGER.info("SELFTEST step 5 suggestions screen ok");
                }
                case 240 -> {
                    if (mc.screen != null) {
                        mc.screen.mouseScrolled(10, 10, 0, -2);
                        mc.screen.mouseScrolled(10, 10, 0, 2);
                    }

                    SmoothSc.LOGGER.info("SELFTEST step 6 suggestions scroll ok");
                }
                case 280 -> {
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
