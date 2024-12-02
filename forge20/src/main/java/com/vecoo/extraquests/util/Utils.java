package com.vecoo.extraquests.util;

import com.vecoo.extraquests.storage.QuestsFactory;
import com.vecoo.extraquests.storage.quests.QuestTimer;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;

public class Utils {
    public static boolean questReset(QuestTimer questTimer, boolean isTimer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(questTimer.getQuestID()));

        if (quest == null) {
            return false;
        }

        if (!QuestsFactory.removeQuestTimer(questTimer) && isTimer) {
            return false;
        }

        quest.forceProgress(file.getOrCreateTeamData(questTimer.getPlayerUUID()), new ProgressChange(file, quest, questTimer.getPlayerUUID()).setReset(true));
        return true;
    }
}