package net.uku3lig.totemcounter.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.uku3lig.totemcounter.ClientEvents;
import net.uku3lig.totemcounter.TotemCounter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TotemConfigScreen extends Screen {
    private final Screen parent;
    private int selectedTab;

    public TotemConfigScreen(Screen parent) {
        super(Component.translatable("totemcounter.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        addRenderableWidget(Button.builder(Component.translatable("totemcounter.config.pop"), button -> {
            selectedTab = 0;
            rebuildWidgets();
        }).bounds(center - 210, 32, 205, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("totemcounter.config.display"), button -> {
            selectedTab = 1;
            rebuildWidgets();
        }).bounds(center + 5, 32, 205, 20).build());

        TotemCounterConfig config = TotemCounterConfig.get();
        int x = center - 105;
        int y = 66;
        if (selectedTab == 0) {
            addToggle("totemcounter.config.enabled", config::isCounterEnabled, config::setCounterEnabled, x, y);
            addToggle("totemcounter.config.pop.separator", config::isSeparator, config::setSeparator, x, y + 25);
            addToggle("totemcounter.config.colors", config::isCounterColors, config::setCounterColors, x, y + 50);
            addToggle("totemcounter.config.tab", config::isShowInTab, config::setShowInTab, x, y + 75);
            addRenderableWidget(Button.builder(Component.translatable("totemcounter.reset"), button ->
                            ClientEvents.resetCounterWithMessage())
                    .bounds(x, y + 110, 210, 20).build());
        } else {
            addToggle("totemcounter.config.enabled", config::isDisplayEnabled, config::setDisplayEnabled, x, y);
            addRenderableWidget(Button.builder(Component.translatable("totemcounter.config.display.position"), button ->
                            minecraft.setScreen(new DisplayPositionSelectScreen(this)))
                    .bounds(x, y + 25, 210, 20).build());
            addToggle("totemcounter.config.display.defaultTotem", config::isUseDefaultTotem, config::setUseDefaultTotem, x, y + 50);
            addToggle("totemcounter.config.colors", config::isDisplayColors, config::setDisplayColors, x, y + 75);
            addToggle("totemcounter.config.display.coloredXpBar", config::isColoredXpBar, config::setColoredXpBar, x, y + 100);
            addToggle("totemcounter.config.display.alwaysShowBar", config::isAlwaysShowBar, config::setAlwaysShowBar, x, y + 125);
            addToggle("totemcounter.config.display.showPopCounter", config::isShowPopCounter, config::setShowPopCounter, x, y + 150);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(center - 100, height - 30, 200, 20).build());
    }

    private void addToggle(String key, BooleanSupplier getter, Consumer<Boolean> setter, int x, int y) {
        Button button = Button.builder(toggleLabel(key, getter.getAsBoolean()), clicked -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            TotemCounterConfig.get().save();
            clicked.setMessage(toggleLabel(key, next));
        }).bounds(x, y, 210, 20).build();
        addRenderableWidget(button);
    }

    private Component toggleLabel(String key, boolean enabled) {
        return Component.translatable(key).append(Component.literal(": "))
                .append(Component.translatable(enabled ? "options.on" : "options.off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
