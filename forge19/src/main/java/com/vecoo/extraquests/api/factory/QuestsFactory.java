package com.vecoo.extraquests.api.factory;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.quests.QuestTimer;

public class QuestsFactory {
    public static void addQuestTimer(QuestTimer questTimer) {
        ExtraQuests.getInstance().getTimerProvider().startTimer(questTimer);
        ExtraQuests.getInstance().getQuestTimerProvider().addQuestTimer(questTimer);
    }

    public static boolean removeQuestTimer(QuestTimer questTimer) {
        if (!ExtraQuests.getInstance().getTimerProvider().removeTimer(questTimer)) {
            return false;
        }

        ExtraQuests.getInstance().getQuestTimerProvider().removeQuestTimer(questTimer);
        return true;
    }
}
