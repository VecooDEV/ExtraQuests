package com.vecoo.extraquests.util;

import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
import com.vecoo.extraquests.storage.quests.TimerStorage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;

public class Utils {
    public static boolean questReset(TimerStorage timer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(timer.getQuestID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("[ExtraQuests] No quest found for " + timer.getQuestID());
            ExtraQuestsFactory.TimerProvider.removeTimerQuests(timer);
            return false;
        }

        TeamData teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(timer.getPlayerUUID()).map(file::getOrCreateTeamData).orElse(file.getOrCreateTeamData(timer.getPlayerUUID()));

        quest.forceProgress(teamData, new ProgressChange(quest, timer.getPlayerUUID()).setReset(true));
        return true;
    }

    public static void startTimer(TimerStorage timer) {
        TaskTimer.builder()
                .delay((timer.getEndTime() - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!Utils.questReset(timer)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsFactory.TimerProvider.removeTimerQuests(timer);
                }).build();
    }
}
