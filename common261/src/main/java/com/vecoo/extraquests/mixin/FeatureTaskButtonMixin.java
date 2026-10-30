package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.TaskProgress;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TaskButton.class)
public abstract class FeatureTaskButtonMixin {
    @Shadow
    @Final
    private Task task;

    @Inject(method = "addMouseOverText", at = @At("TAIL"))
    private void extraQuests$optionsTooltip(@NotNull TooltipList list, @NotNull CallbackInfo ci) {
        if (ClientQuestFile.exists() && ClientQuestFile.getInstance().selfTeamData != null) {
            TaskProgress.tooltip(this.task, list, ClientQuestFile.getInstance().selfTeamData);
        }
    }
}
