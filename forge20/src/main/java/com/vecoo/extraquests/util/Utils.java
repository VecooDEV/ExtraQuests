package com.vecoo.extraquests.util;

import com.vecoo.extraquests.timer.QuestTimer;
import com.vecoo.extraquests.timer.QuestTimerFactory;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;

public class Utils {
    public static void timerExpired(QuestTimer questTimer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(questTimer.getQuestID());

        if (quest != null) {
            quest.forceProgress(file.getOrCreateTeamData(questTimer.getPlayerUUID()), new ProgressChange(file, quest, questTimer.getPlayerUUID()).setReset(true));
        }
        QuestTimerFactory.removeQuestTimer(questTimer);
    }
}