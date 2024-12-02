package com.vecoo.extraquests.util;

import com.vecoo.extraquests.storage.quests.QuestTimer;
import com.vecoo.extraquests.storage.QuestsFactory;
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
        QuestsFactory.removeQuestTimer(questTimer);
    }
}