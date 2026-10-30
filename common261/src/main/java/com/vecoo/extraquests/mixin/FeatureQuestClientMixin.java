package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.quest.QuestSettings;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Quest.class)
public abstract class FeatureQuestClientMixin {
    @Inject(method = "fillConfigGroup", at = @At("TAIL"))
    private void extraQuests$editCalendar(@NotNull EditableConfigGroup config, @NotNull CallbackInfo ci) {
        QuestSettings.get((Quest) (Object) this).fillConfigGroup(config);
    }
}
