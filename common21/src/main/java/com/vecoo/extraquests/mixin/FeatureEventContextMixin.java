package com.vecoo.extraquests.mixin;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftbquests.FTBQuestsEventHandler;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(FTBQuestsEventHandler.class)
public abstract class FeatureEventContextMixin {
    @Unique
    private static void extraQuests$withPlayer(@NotNull Optional<TeamData> data, @NotNull Consumer<TeamData> work, @Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer && ServerQuestFile.INSTANCE != null) {
            ServerQuestFile.INSTANCE.withPlayerContext(serverPlayer, () -> data.ifPresent(work));
        } else {
            data.ifPresent(work);
        }
    }

    @Redirect(method = "playerKill", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private void extraQuests$killContext(@NotNull Optional<TeamData> data, @NotNull Consumer<TeamData> work, @NotNull LivingEntity entity, @NotNull DamageSource source) {
        extraQuests$withPlayer(data, work, source.getEntity() instanceof ServerPlayer player ? player : null);
    }

    @Redirect(method = "playerInteract", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private void extraQuests$interactionContext(@NotNull Optional<TeamData> data, @NotNull Consumer<TeamData> work, @NotNull Player player, @NotNull Either<BlockPos, Entity> target) {
        extraQuests$withPlayer(data, work, player);
    }
}
