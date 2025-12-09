package com.vecoo.extraquests.util;

import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
import com.vecoo.extraquests.storage.QuestTimer;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static boolean questReset(@NotNull QuestTimer questTimer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(questTimer.questID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for {}.", questTimer.questID());
            ExtraQuestsFactory.QuestTimerProvider.removeQuestTimer(questTimer);
            return false;
        }

        TeamData teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(questTimer.playerUUID())
                .map(file::getOrCreateTeamData).orElse(file.getOrCreateTeamData(questTimer.playerUUID()));

        quest.forceProgress(teamData, new ProgressChange(file, quest, questTimer.playerUUID()).setReset(true));
        return true;
    }

    public static void startQuestTimer(@NotNull QuestTimer questTimer) {
        TaskTimer.builder()
                .delay((questTimer.endTime() - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!Utils.questReset(questTimer)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsFactory.QuestTimerProvider.removeQuestTimer(questTimer);
                }).build();
    }
}
