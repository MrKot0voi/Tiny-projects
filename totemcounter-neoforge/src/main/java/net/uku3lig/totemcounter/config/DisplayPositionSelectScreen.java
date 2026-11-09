package net.uku3lig.totemcounter.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.uku3lig.totemcounter.TotemCounter;

public class DisplayPositionSelectScreen extends Screen {
    private final Screen parent;
    private final TotemCounterConfig config;
    private int ticksElapsed;

    public DisplayPositionSelectScreen(Screen parent) {
        super(Component.translatable("totemcounter.config.display.position"));
        this.parent = parent;
        this.config = TotemCounterConfig.get();
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("totemcounter.config.display.position.reset"), button -> {
            config.setX(-1);
            config.setY(-1);
            config.save();
        }).bounds(width / 2 - 105, height - 55, 210, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 105, height - 30, 210, 20).build());
    }

    @Override
    public void tick() {
        ticksElapsed = (ticksElapsed + 1) % 100;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("totemcounter.config.display.position.hint"),
                width / 2, 29, 0xAAAAAA);

        int x = config.getX() == -1 ? width / 2 - 8 : config.getX();
        int y = config.getY() == -1 ? height / 2 - 8 : config.getY();
        renderTotem(graphics, x, y);
        Component preview = Component.literal(String.valueOf(ticksElapsed / 4));
        int color = TotemCounter.getTotemColor(ticksElapsed / 10);
        graphics.drawString(font, preview, x + 18, y + 4, color);
    }

    private void renderTotem(GuiGraphics graphics, int x, int y) {
        if (config.isUseDefaultTotem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TotemCounter.DEFAULT_TOTEM, x, y,
                    0, 0, 16, 16, 16, 16);
        } else {
            graphics.renderItem(TotemCounter.TOTEM, x, y);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && event.y() > 48 && event.y() < height - 80) {
            config.setX((int) event.x() - 8);
            config.setY((int) event.y() - 8);
            config.save();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
