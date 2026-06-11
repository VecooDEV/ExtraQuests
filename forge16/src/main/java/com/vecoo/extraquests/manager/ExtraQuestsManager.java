package com.vecoo.extraquests.manager;

import com.vecoo.extralib.scheduler.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.service.ExtraQuestsService;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ExtraQuestsManager {
    public static void startQuestTimer(@Nonnull UUID playerUUID, @Nonnull String questID, long endTime) {
        TaskTimer.builder()
                .delay((endTime - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!questReset(playerUUID, questID)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsService.removeQuestTimer(playerUUID, questID);
                }).build();
    }

    public static boolean questReset(@Nonnull UUID playerUUID, @Nonnull String questID) {
        val file = ServerQuestFile.INSTANCE;
        val quest = file.getQuest(file.getID(questID));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for {}.", quest);
            ExtraQuestsService.removeQuestTimer(playerUUID, questID);
            return false;
        }

        val progressChange = new ProgressChange(file);
        progressChange.origin = quest;
        progressChange.player = playerUUID;

        quest.forceProgress(file.getData(FTBTeamsAPI.getPlayerTeamID(playerUUID)), progressChange);
        return true;
    }
}
