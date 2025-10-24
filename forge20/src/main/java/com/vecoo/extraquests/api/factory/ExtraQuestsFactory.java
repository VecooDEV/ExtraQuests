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
        public static Set<TimerStorage> getStorage() {
            return ExtraQuests.getInstance().getTimerProvider().getStorage();
        }

        public static boolean addTimerQuests(@NotNull UUID playerUUID, @NotNull String questID, int seconds) {
            TimerStorage timer = new TimerStorage(playerUUID, questID, seconds);

            if (!ExtraQuests.getInstance().getTimerProvider().addTimer(timer)) {
                return false;
            }

            Utils.startTimer(timer);
            return true;
        }

        public static boolean removeTimerQuests(@NotNull TimerStorage timer) {
            return ExtraQuests.getInstance().getTimerProvider().removeTimer(timer);
        }
    }
}