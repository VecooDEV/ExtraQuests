package com.vecoo.extraquests.timer;

import com.vecoo.extraquests.ExtraQuests;

public class QuestTimerFactory {
    public static void addQuestTimer(QuestTimer questTimer) {
        ExtraQuests.getInstance().getQuestTimerProvider().addQuestTimer(questTimer);
    }

    public static void removeQuestTimer(QuestTimer questTimer) {
        ExtraQuests.getInstance().getQuestTimerProvider().removeQuestTimer(questTimer);
    }
}
