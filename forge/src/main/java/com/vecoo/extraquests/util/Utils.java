package com.vecoo.extraquests.util;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.timer.QuestTimerListing;

public class Utils {

    public static void removeAllTimers() {
        for (QuestTimerListing listing : ExtraQuests.getInstance().getTimerProvider().getTimers()) {
            ExtraQuests.getInstance().getTimerProvider().deleteQuestTimer(listing);
        }
    }
}