package com.vecoo.extraquests.api.factory;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.TimerStorage;
import com.vecoo.extraquests.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ExtraQuestsFactory {
    public static class TimerProvider {
        @NotNull
        public static Set<TimerStorage> storage() {
            return ExtraQuests.instance().timerProvider().storage();
        }

        public static boolean addTimerQuests(@NotNull UUID playerUUID, @NotNull String questID, int seconds) {
            TimerStorage timer = new TimerStorage(playerUUID, questID, seconds);

            if (!ExtraQuests.instance().timerProvider().addTimer(timer)) {
                return false;
            }

            Utils.startQuestTimer(timer);
            return true;
        }

        public static boolean removeTimerQuests(@NotNull TimerStorage timerStorage) {
            return ExtraQuests.instance().timerProvider().removeTimer(timerStorage);
        }
    }
}