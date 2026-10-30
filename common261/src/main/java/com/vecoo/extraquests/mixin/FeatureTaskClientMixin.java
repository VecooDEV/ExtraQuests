package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.task.TaskSettings;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Task.class)
public abstract class FeatureTaskClientMixin {
    @Inject(method = "fillConfigGroup", at = @At("TAIL"))
    private void extraQuests$editOptions(@NotNull EditableConfigGroup config, @NotNull CallbackInfo ci) {
        TaskSettings.get((Task) (Object) this).fillConfigGroup(config, (Task) (Object) this);
    }
}
