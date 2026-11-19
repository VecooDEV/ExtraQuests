package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.reward.AttributeReward;
import com.vecoo.extraquests.reward.CalendarStreakReward;
import com.vecoo.extraquests.runtime.QuestFeatures;
import com.vecoo.extraquests.runtime.TaskProgress;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import lombok.val;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = TeamData.class, remap = false)
public abstract class FeatureTeamDataMixin {
    @Inject(method = "canStartTasks", at = @At("HEAD"), cancellable = true)
    private void extraQuests$calendarAvailable(@NotNull Quest quest, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!QuestFeatures.canStart((TeamData) (Object) this, quest)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setProgress", at = @At("HEAD"), cancellable = true)
    private void extraQuests$calendarProgress(@NotNull Task task, long progress, @NotNull CallbackInfo ci) {
        if (!TaskProgress.internal(task) && progress > 0L && !QuestFeatures.canStart((TeamData) (Object) this, task.getQuest())) {
            ci.cancel();
        } else if (TaskProgress.set((TeamData) (Object) this, task, progress)) {
            ci.cancel();
        }
    }

    @Inject(method = "addProgress", at = @At("HEAD"), cancellable = true)
    private void extraQuests$extendedIncrement(@NotNull Task task, long amount, @NotNull CallbackInfo ci) {
        if (TaskProgress.add((TeamData) (Object) this, task, amount)) {
            ci.cancel();
        }
    }

    @Inject(method = "getProgress(J)J", at = @At("HEAD"), cancellable = true)
    private void extraQuests$sharedProgress(long task, @NotNull CallbackInfoReturnable<Long> cir) {
        val progress = TaskProgress.sharedProgress((TeamData) (Object) this, task);

        if (progress != null) {
            cir.setReturnValue(progress);
        }
    }

    @Inject(method = "markRewardAsClaimed", at = @At("HEAD"), cancellable = true)
    private void extraQuests$sharedReward(@NotNull UUID player, @NotNull Reward reward, long time, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!QuestFeatures.canClaim(player, reward) || !QuestFeatures.canStart((TeamData) (Object) this, reward.getQuest())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            target = @Desc(value = "claimReward", args = {ServerPlayer.class, Reward.class, boolean.class, long.class}),
            at = @At("HEAD"),
            cancellable = true
    )
    private void extraQuests$validateReward(@NotNull ServerPlayer player, @NotNull Reward reward, boolean notify, long time, @NotNull CallbackInfo ci) {
        if (reward instanceof AttributeReward attribute && !attribute.canClaim(player)
                || reward instanceof CalendarStreakReward streak && !streak.canClaim((TeamData) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "getClaimType", at = @At("RETURN"), cancellable = true)
    private void extraQuests$sharedClaimType(@NotNull UUID player, @NotNull Reward reward, @NotNull CallbackInfoReturnable<RewardClaimType> cir) {
        if (cir.getReturnValue() == RewardClaimType.CAN_CLAIM && (!QuestFeatures.canClaim(player, reward)
                || !QuestFeatures.canStart((TeamData) (Object) this, reward.getQuest()))) {
            cir.setReturnValue(RewardClaimType.CANT_CLAIM);
        }
    }
}
