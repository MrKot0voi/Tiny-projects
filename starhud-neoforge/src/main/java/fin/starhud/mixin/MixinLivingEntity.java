package fin.starhud.mixin;

import fin.starhud.CrashGuard;
import fin.starhud.helper.StatusEffectAttribute;
import fin.starhud.hud.HUDComponent;
import fin.starhud.hud.HUDId;
import fin.starhud.hud.implementation.statuseffect.NegativeEffectHUD;
import fin.starhud.hud.implementation.statuseffect.PositiveEffectHUD;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
    public void onSetMobEffect(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        try {
            PositiveEffectHUD pos = (PositiveEffectHUD) HUDComponent.getInstance().getHUD(HUDId.POSITIVE_EFFECT);
            NegativeEffectHUD neg = (NegativeEffectHUD) HUDComponent.getInstance().getHUD(HUDId.NEGATIVE_EFFECT);
            if (pos == null || neg == null || effect == null) return;
            if (!pos.shouldRender() && !neg.shouldRender()) return;
            StatusEffectAttribute attr = StatusEffectAttribute.getStatusEffectAttribute(effect);
            if (StatusEffectAttribute.shouldUpdate(effect, attr)) {
                StatusEffectAttribute.updateStatusEffectAttribute(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient());
            }
        } catch (Throwable t) {
            CrashGuard.report(t, "MixinLivingEntity.onSetMobEffect");
        }
    }

    @Inject(method = "removeEffectNoUpdate", at = @At("RETURN"))
    public void onMobEffectRemoved(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        StatusEffectAttribute.removeStatusEffectAttribute(effect);
    }
}
