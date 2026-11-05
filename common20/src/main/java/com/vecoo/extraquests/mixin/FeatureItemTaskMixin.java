package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.TaskProgress;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemTask.class, priority = 1100, remap = false)
public abstract class FeatureItemTaskMixin {
    @Redirect(method = "submitTask", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/TeamData;getProgress(Ldev/ftb/mods/ftbquests/quest/task/Task;)J"))
    private long extraQuests$compareInventorySample(@NotNull TeamData data, @NotNull Task task, @NotNull TeamData parent, @NotNull ServerPlayer player, @NotNull ItemStack stack) {
        return TaskProgress.comparison(task, data, player);
    }
}
