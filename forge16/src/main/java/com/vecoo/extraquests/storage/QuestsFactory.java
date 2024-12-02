package com.vecoo.extraquests.storage;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.quests.QuestTimer;

public class QuestsFactory {
    public static void addQuestTimer(QuestTimer questTimer) {
        ExtraQuests.getInstance().getTimerProvider().startTimer(questTimer);
        ExtraQuests.getInstance().getQuestTimerProvider().addQuestTimer(questTimer);
    }

    public static void removeQuestTimer(QuestTimer questTimer) {
        ExtraQuests.getInstance().getTimerProvider().removeTimer(questTimer);
        ExtraQuests.getInstance().getQuestTimerProvider().removeQuestTimer(questTimer);
    }
}
