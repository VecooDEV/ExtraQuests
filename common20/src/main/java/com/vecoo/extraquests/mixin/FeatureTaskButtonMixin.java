package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.TaskProgress;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TaskButton.class, remap = false)
public abstract class FeatureTaskButtonMixin {
    @Shadow
    Task task;

    @Inject(method = "addMouseOverText", at = @At("TAIL"))
    private void extraQuests$optionsTooltip(@NotNull TooltipList list, @NotNull CallbackInfo ci) {
        if (ClientQuestFile.INSTANCE != null && ClientQuestFile.INSTANCE.selfTeamData != null) {
            TaskProgress.tooltip(this.task, list, ClientQuestFile.INSTANCE.selfTeamData);
        }
    }
}
