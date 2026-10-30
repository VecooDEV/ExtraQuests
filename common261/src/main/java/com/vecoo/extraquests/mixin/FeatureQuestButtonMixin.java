package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.network.QuestClientState;
import com.vecoo.extraquests.quest.QuestSettings;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuestButton.class)
public abstract class FeatureQuestButtonMixin {
    @Shadow
    @Final
    private Quest quest;

    @Inject(method = "addMouseOverText", at = @At("TAIL"))
    private void extraQuests$calendarTooltip(@NotNull TooltipList list, @NotNull CallbackInfo ci) {
        if (QuestSettings.get(this.quest).isCalendarEnabled()) {
            val state = QuestClientState.quest(this.quest.id);

            list.translate(state.getBooleanOr("active", false) ? "extraquests.calendar.active" : "extraquests.calendar.inactive");
            list.translate("extraquests.calendar.streak", state.getIntOr("streak", 0));
            if (state.contains("next_reset")) {
                list.translate("extraquests.calendar.remaining", Math.max(0L, (state.getLongOr("next_reset", 0L) - System.currentTimeMillis()) / 1000L));
            }
        }
    }
}
