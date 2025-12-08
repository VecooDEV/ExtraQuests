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
    public static boolean questReset(@NotNull QuestTimer timerStorage) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(timerStorage.questID()));

        if (quest == null) {
            ExtraQuests.logger().error("No quest found for {}.", timerStorage.questID());
            ExtraQuestsFactory.QuestTimerProvider.remove(timerStorage);
            return false;
        }

        TeamData teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(timerStorage.playerUUID())
                .map(file::getOrCreateTeamData).orElse(file.getOrCreateTeamData(timerStorage.playerUUID()));

        quest.forceProgress(teamData, new ProgressChange(file, quest, timerStorage.playerUUID()).setReset(true));
        return true;
    }

    public static void startQuestTimer(@NotNull QuestTimer timerStorage) {
        TaskTimer.builder()
                .delay((timerStorage.endTime() - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!Utils.questReset(timerStorage)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsFactory.QuestTimerProvider.remove(timerStorage);
                }).build();
    }
}
