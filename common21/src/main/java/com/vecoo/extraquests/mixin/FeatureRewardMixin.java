package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.QuestFeatures;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Reward.class)
public abstract class FeatureRewardMixin {
    @Inject(method = "tryClaim", at = @At("HEAD"), cancellable = true)
    private void extraQuests$sharedEligibility(@NotNull ServerPlayer player, boolean notify, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!QuestFeatures.canClaim(player.getUUID(), (Reward) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
