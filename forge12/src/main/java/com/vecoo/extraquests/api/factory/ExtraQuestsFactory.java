package com.vecoo.extraquests.api.factory;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.TimerStorage;
import com.vecoo.extraquests.util.Utils;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

public class ExtraQuestsFactory {
    public static class TimerProvider {
        @Nonnull
        public static Set<TimerStorage> getStorage() {
            return ExtraQuests.getInstance().getTimerProvider().getStorage();
        }

        public static boolean addTimerQuests(@Nonnull UUID playerUUID, @Nonnull String questID, int seconds) {
            TimerStorage timer = new TimerStorage(playerUUID, questID, seconds);

            if (!ExtraQuests.getInstance().getTimerProvider().addTimer(timer)) {
                return false;
            }

            Utils.startTimer(timer);
            return true;
        }

        public static boolean removeTimerQuests(@Nonnull TimerStorage timer) {
            return ExtraQuests.getInstance().getTimerProvider().removeTimer(timer);
        }
    }
}