package com.vecoo.extraquests.api.factory;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.quests.TimerStorage;
import com.vecoo.extraquests.util.Utils;

import java.util.Set;
import java.util.UUID;

public class ExtraQuestsFactory {
    public static class TimerProvider {
        public static Set<TimerStorage> getTimerQuests() {
            return ExtraQuests.getInstance().getTimerProvider().getTimers();
        }

        public static boolean addTimerQuests(UUID playerUUID, String questID, int seconds) {
            TimerStorage timer = new TimerStorage(playerUUID, questID, seconds);

            if (!ExtraQuests.getInstance().getTimerProvider().addTimer(timer)) {
                return false;
            }

            Utils.startTimer(timer);
            return true;
        }

        public static boolean removeTimerQuests(TimerStorage timer) {
            return ExtraQuests.getInstance().getTimerProvider().removeTimer(timer);
        }
    }
}