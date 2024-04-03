package com.vecoo.extraquests.util;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.timer.QuestTimerListing;

public class Utils {

    public static void removeAllTimers() {
        for (QuestTimerListing listing : ExtraQuests.getTimerProvider().getTimers()) {
            ExtraQuests.getTimerProvider().deleteQuestTimer(listing);
        }
    }
}