package com.vecoo.extraquests.util;

import com.vecoo.extraquests.timer.QuestTimer;
import com.vecoo.extraquests.timer.QuestTimerFactory;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;

public class Utils {
    public static void timerExpired(QuestTimer questTimer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        QuestObject questObject = file.get(file.getID(questTimer.getQuest()));

        if (questObject != null) {
            ProgressChange progressChange = new ProgressChange(file);
            progressChange.origin = questObject;

            questObject.forceProgress(file.getData(FTBTeamsAPI.getPlayerTeamID(questTimer.getPlayerUUID())), progressChange);
        }
        QuestTimerFactory.removeQuestTimer(questTimer);
    }
}