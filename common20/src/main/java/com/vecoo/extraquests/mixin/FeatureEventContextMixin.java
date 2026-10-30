package com.vecoo.extraquests.mixin;

import dev.ftb.mods.ftbquests.FTBQuestsEventHandler;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import lombok.val;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FTBQuestsEventHandler.class, remap = false)
public abstract class FeatureEventContextMixin {
    @Redirect(method = "playerKill", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/task/KillTask;kill(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void extraQuests$killContext(@NotNull KillTask task, @NotNull TeamData data, @NotNull LivingEntity victim,
                                       @NotNull LivingEntity entity, @NotNull DamageSource source) {
        val file = ServerQuestFile.INSTANCE;

        if (source.getEntity() instanceof ServerPlayer player && file != null) {
            file.withPlayerContext(player, () -> task.kill(data, victim));
        } else {
            task.kill(data, victim);
        }
    }
}
