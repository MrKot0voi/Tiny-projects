package walksy.shieldfixes.manager;

import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.mods.data.ShieldStateDataTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import walksy.shieldfixes.CrashGuard;
import walksy.shieldfixes.interfaze.ILivingEntity;

public final class ShieldStateManager {
    private static final int IDLE_USE_TIME = 72000;

    private ShieldStateManager() {
    }

    private static ShieldStateDataTracker getTrackerSafe() {
        try {
            WalksyLib lib = WalksyLib.getInstance();
            if (lib == null) {
                return null;
            }
            return lib.getShieldStateData();
        } catch (Throwable t) {
            CrashGuard.report("ShieldStateManager.getTrackerSafe", t);
            return null;
        }
    }

    public static void syncRemoteShieldUse() {
        CrashGuard.flushIfReady();
        try {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.level == null) {
                return;
            }
            final ShieldStateDataTracker state = getTrackerSafe();
            if (state == null) {
                return;
            }
            for (final Player player : minecraft.level.players()) {
                if (player == null || player == minecraft.player || !state.isUsingShield(player, false)) {
                    continue;
                }
                InteractionHand hand = player.getUsedItemHand();
                if (hand == null) {
                    hand = player.getOffhandItem().is(Items.SHIELD) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                }
                ItemStack activeStack = player.getItemInHand(hand);
                ((ILivingEntity) player).setActiveItem(activeStack != null ? activeStack : ItemStack.EMPTY);
                ((ILivingEntity) player).setItemUseTime(IDLE_USE_TIME - state.shieldUseTicks(player));
            }
        } catch (Throwable t) {
            CrashGuard.report("ShieldStateManager.syncRemoteShieldUse", t);
        }
    }

    public static boolean isUsingShield(final Player player, final boolean delay) {
        if (player == null) {
            return false;
        }
        try {
            ShieldStateDataTracker state = getTrackerSafe();
            return state != null && state.isUsingShield(player, delay);
        } catch (Throwable t) {
            CrashGuard.report("ShieldStateManager.isUsingShield", t);
            return false;
        }
    }

    public static boolean isUsingShield(final Player player, final Vec3 source, final boolean delay) {
        if (player == null || !isUsingShield(player, delay)) {
            return false;
        }
        if (source == null) {
            return true;
        }
        try {
            final Vec3 viewVector = player.getViewVector(1);
            final Vec3 vectorTo = source.vectorTo(player.position()).normalize();
            final Vec3 flat = new Vec3(vectorTo.x, 0.0, vectorTo.z);
            return flat.dot(viewVector) < 0.0;
        } catch (Throwable t) {
            CrashGuard.report("ShieldStateManager.isUsingShield(Vec3)", t);
            return false;
        }
    }

    public static boolean disablesShield(final Player player) {
        if (player == null) {
            return false;
        }
        try {
            ShieldStateDataTracker state = getTrackerSafe();
            return state != null && state.disablesShield(player);
        } catch (Throwable t) {
            CrashGuard.report("ShieldStateManager.disablesShield", t);
            return false;
        }
    }
}
