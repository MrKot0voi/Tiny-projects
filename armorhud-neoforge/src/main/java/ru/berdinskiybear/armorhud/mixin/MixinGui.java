package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.CrashGuard;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import static ru.berdinskiybear.armorhud.ArmorHudMod.*;

@Mixin(Gui.class)
public abstract class MixinGui {
    @Shadow
    @Final
    private RandomSource random;

    @Unique
    private static final Identifier WARNING_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "warn.png");

    @Shadow
    protected abstract void renderSlot(GuiGraphics graphics, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed);

    @Shadow
    public abstract Font getFont();

    @Shadow
    @Final
    private static Identifier HOTBAR_SPRITE;

    @Shadow
    @Final
    private static Identifier HOTBAR_OFFHAND_LEFT_SPRITE;

    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    public void renderArmorHud(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        Profiler.get().push(MOD_ID);
        try {
            drawArmorHud(graphics, tickCounter);
        } catch (Throwable t) {
            CrashGuard.report("MixinGui.renderArmorHud", t);
        } finally {
            Profiler.get().pop();
        }
    }

    @Unique
    private void drawArmorHud(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (graphics == null) return;
        ArmorHudConfig config = getManager().getConfig();
        if (!config.isEnabled()) return;

        Player player = getCameraPlayer();
        if (player == null) return;

        final Optional<Rect2i> rect = getWidgetRect(graphics, player);
        if (rect.isEmpty()) return;

        List<ItemStack> armorItems = getArmorItems(player);
        if (armorItems.isEmpty()) return;
        if (config.isReversed()) {
            armorItems = armorItems.reversed();
        }

        final int textureWidth = SIZE + ((armorItems.size() - 1) * STEP);

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(rect.get().getX(), rect.get().getY());

            if (config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
                graphics.pose().rotate(Mth.HALF_PI).translate(0, -SIZE);
            }

            int color = 0xFFFFFFFF;

            switch (config.getStyle()) {
                case HOTBAR -> {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 0, 0, 0, 0, textureWidth - 3, SIZE, color);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 182 - 3, 0, textureWidth - 3, 0, 3, SIZE, color);
                }
                case ROUNDED_CORNERS -> {
                    if (armorItems.size() > 1) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, 3, SIZE, color);
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 3, 0, 3, 0, textureWidth - 6, SIZE, color);
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, SIZE - 3, 1, textureWidth - 3, 0, 3, SIZE, color);
                    } else {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE, SIZE, color);
                    }
                }
                case ROUNDED -> {
                    if (armorItems.size() > 1) {
                        int borderWidth = (SIZE - STEP) / 2;
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE - borderWidth, SIZE, color);
                        for (int i = 1; i < armorItems.size() - 1; i++) {
                            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, borderWidth, 1, borderWidth + i * STEP, 0, STEP, SIZE, color);
                        }
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 1, 1, textureWidth - STEP - borderWidth, 0, SIZE - borderWidth, SIZE, color);
                    } else {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE, SIZE, color);
                    }
                }
                case NONE -> {}
            }
        } finally {
            graphics.pose().popMatrix();
        }

        Map<EquipmentSlot, Identifier> emptySlotTextures = null;
        try {
            emptySlotTextures = InventoryMenuAccessor.getTEXTURE_EMPTY_SLOTS();
        } catch (Throwable ignored) {
        }

        for (int i = 0; i < armorItems.size(); i++) {
            ItemStack stack = armorItems.get(i);
            if (stack == null) continue;
            int x = rect.get().getX();
            int y = rect.get().getY();

            switch (config.getOrientation()) {
                case HORIZONTAL -> x += (STEP * i);
                case VERTICAL -> y += (STEP * i);
            }

            if (config.isIconsShown() && config.getWidgetShown().shouldDrawEmptySlots() && stack.isEmpty() && emptySlotTextures != null) {
                int slotIndex = config.isReversed() ? (SLOT_IDS.length - 1 - i) : i;
                if (slotIndex >= 0 && slotIndex < SLOT_IDS.length) {
                    Identifier identifier = emptySlotTextures.get(SLOT_IDS[slotIndex]);
                    if (identifier != null) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x + 3, y + 3, 16, 16);
                    }
                }
            }

            this.renderSlot(graphics, x + 3, y + 3, tickCounter, player, stack, i + 1);

            ArmorHudConfig.Side extrasSide = config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR ? config.getSide() : config.getSide().getOpposite();

            if (config.getAnchor().isTop() && config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                y += SIZE;
            } else if (extrasSide == ArmorHudConfig.Side.RIGHT && config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
                x += SIZE;
            }

            if (config.getDurabilityDisplay() != ArmorHudConfig.DurabilityDisplay.BAR && !stack.isEmpty() && stack.isDamageableItem() && stack.getMaxDamage() > 0 && this.getFont() != null) {
                String dura = switch (config.getDurabilityDisplay()) {
                    case NUMERIC -> String.valueOf(stack.getMaxDamage() - stack.getDamageValue());
                    case PERCENTAGE -> {
                        double percentage = 1.0 - ((double) stack.getDamageValue() / stack.getMaxDamage());
                        yield (int) Math.floor(percentage * 100) + "%";
                    }
                    case BAR -> "";
                };
                int textHeight = this.getFont().lineHeight;

                if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                    if (!config.getAnchor().isTop()) y -= textHeight;
                    graphics.drawCenteredString(this.getFont(), dura, x + (SIZE / 2), y, ARGB.opaque(stack.getBarColor()));
                    if (config.getAnchor().isTop()) y += textHeight;
                } else {
                    int textWidth = this.getFont().width(dura) + 2;
                    int textY = (SIZE - textHeight) / 2;

                    if (extrasSide == ArmorHudConfig.Side.LEFT) x -= textWidth;
                    graphics.drawString(this.getFont(), dura, x + 1, y + textY, ARGB.opaque(stack.getBarColor()));
                    if (extrasSide == ArmorHudConfig.Side.RIGHT) x += textWidth;
                }
            }

            if (config.isWarningShown() && shouldShowWarning(stack)) {
                int intensity = config.getWarningBobIntensity();
                if (intensity > 0 && this.random != null) {
                    y += (int) (this.random.nextInt(intensity) - Math.ceil(intensity / 2F));
                }

                if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                    if (!config.getAnchor().isTop()) y -= WARNING_SIZE + 2;

                    int warnX = (SIZE - WARNING_SIZE) / 2;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + warnX, y + 1, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
                } else {
                    if (extrasSide == ArmorHudConfig.Side.LEFT) x -= WARNING_SIZE + 2;

                    int warnY = (SIZE - WARNING_SIZE) / 2;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + 1, y + warnY, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
                }
            }
        }
    }

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        try {
            ArmorHudConfig config = getManager().getConfig();
            if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                    || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

            Player player = getCameraPlayer();
            if (player == null) return;

            Optional<Rect2i> rect = getEffectiveWidgetRect(graphics, player);
            if (rect.isEmpty()) return;

            shiftRef.set(rect.get().getY() + rect.get().getHeight());
        } catch (Throwable t) {
            CrashGuard.report("MixinGui.calculateStatusEffectIconsOffset", t);
        }
    }

    @ModifyVariable(method = "renderEffects", at = @At(value = "STORE"), ordinal = 3)
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}
