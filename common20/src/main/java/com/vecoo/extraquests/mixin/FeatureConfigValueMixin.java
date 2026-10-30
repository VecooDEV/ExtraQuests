package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.client.config.ConfigConditions;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ConfigValue.class, remap = false)
public abstract class FeatureConfigValueMixin {
    @Inject(method = "getCanEdit", at = @At("RETURN"), cancellable = true)
    private void extraQuests$enabled(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && !ConfigConditions.isEnabled((ConfigValue<?>) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
