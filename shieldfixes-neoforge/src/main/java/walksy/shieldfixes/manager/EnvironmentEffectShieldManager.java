package walksy.shieldfixes.manager;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import walksy.shieldfixes.CrashGuard;
import walksy.shieldfixes.config.ShieldFixesConfig;

public class EnvironmentEffectShieldManager {

    private static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public void onExplosion(final WorldExplosion explosion) {
        if (explosion == null || explosion.pos() == null) {
            return;
        }
        Minecraft mc = minecraft();
        if (mc == null) {
            return;
        }
        if (!mc.isSameThread()) {
            Vec3 pos = explosion.pos();
            float power = explosion.power();
            mc.execute(() -> handleClientExplosion(pos, power));
            return;
        }
        handleClientExplosion(explosion.pos(), explosion.power());
    }

    private void handleClientExplosion(final Vec3 pos, final float power) {
        try {
            Minecraft mc = minecraft();
            if (mc == null || mc.level == null || mc.player == null) {
                return;
            }
            WorldExplosion clientExplosion = new WorldExplosion(pos, mc.level, power);
            for (final Player affectedPlayer : getNearPlayers(clientExplosion)) {
                if (affectedPlayer == null || !affectedPlayer.isAlive()) {
                    continue;
                }
                final double distance = Math.sqrt(affectedPlayer.distanceToSqr(pos));
                if (distance < clientExplosion.getMaxDistance()
                        && ShieldStateManager.isUsingShield(affectedPlayer, pos, ShieldFixesConfig.factorDelay())) {
                    this.sound(affectedPlayer, shieldBlock());
                }
            }
        } catch (Throwable t) {
            CrashGuard.report("EnvironmentEffectShieldManager.handleClientExplosion", t);
        }
    }

    public void onDisable(final LivingEntity livingEntity) {
        if (livingEntity == null) return;
        try {
            this.sound(livingEntity, shieldBreak());
        } catch (Throwable t) {
            CrashGuard.report("EnvironmentEffectShieldManager.onDisable", t);
        }
    }

    public void onAttack(final Player target) {
        if (target == null) return;
        try {
            Minecraft mc = minecraft();
            if (mc == null || mc.player == null) return;
            if (ShieldStateManager.isUsingShield(target, mc.player.position(), true) && !ShieldStateManager.disablesShield(mc.player)) {
                this.sound(target, shieldBlock());
            }
        } catch (Throwable t) {
            CrashGuard.report("EnvironmentEffectShieldManager.onAttack", t);
        }
    }

    private static SoundEvent shieldBlock() {
        return SoundEvents.SHIELD_BLOCK.value();
    }

    private static SoundEvent shieldBreak() {
        return SoundEvents.SHIELD_BREAK.value();
    }

    private static List<Player> getNearPlayers(final WorldExplosion explosion) {
        if (explosion.level() == null) return List.of();
        return explosion.level().getEntitiesOfClass(Player.class, explosion.getBoundingBox(), player -> true);
    }

    private void sound(final LivingEntity entity, final SoundEvent soundEvent) {
        Minecraft mc = minecraft();
        if (mc == null || mc.level == null || entity == null || soundEvent == null) {
            return;
        }
        mc.level.playSound(entity, entity.getX(), entity.getY(), entity.getZ(), soundEvent, entity.getSoundSource(), 1F, 0.8F + mc.level.getRandom().nextFloat() * 0.4F);
    }

    public record WorldExplosion(Vec3 pos, Level level, float power) {
        public float getMaxDistance() {
            return this.power * 2;
        }

        public AABB getBoundingBox() {
            final float maxDistance = this.getMaxDistance();
            final int x1 = Mth.floor(this.pos.x - maxDistance - 1.0);
            final int x2 = Mth.floor(this.pos.x + maxDistance + 1.0);
            final int y1 = Mth.floor(this.pos.y - maxDistance - 1.0);
            final int y2 = Mth.floor(this.pos.y + maxDistance + 1.0);
            final int z1 = Mth.floor(this.pos.z - maxDistance - 1.0);
            final int z2 = Mth.floor(this.pos.z + maxDistance + 1.0);
            return new AABB(x1, y1, z1, x2, y2, z2);
        }
    }
}
