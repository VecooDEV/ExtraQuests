package com.vecoo.extraquests.api.factory;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.quests.TimerStorage;
import com.vecoo.extraquests.util.TaskTimerUtils;
import com.vecoo.extraquests.util.Utils;

import java.util.Set;
import java.util.UUID;

public class ExtraQuestsFactory {
    public static class TimerProvider {
        public static Set<TimerStorage> getTimerQuests() {
            return ExtraQuests.getInstance().getTimerProvider().getTimers();
        }

        public static void addTimerQuests(UUID playerUUID, String questID, int time) {
            TimerStorage timer = new TimerStorage(playerUUID, questID, time);

            ExtraQuests.getInstance().getTimerProvider().addTimer(timer);
            startTimer(timer);
        }

        public static void removeTimerQuests(TimerStorage timer) {
            ExtraQuests.getInstance().getTimerProvider().removeTimer(timer);
        }

        public static void startTimer(TimerStorage timer) {
            TaskTimerUtils.builder()
                    .delay((timer.getEndTime() - System.currentTimeMillis()) / 50L)
                    .consume(task -> {
                        if (!Utils.questReset(timer)) {
                            task.cancel();
                            return;
                        }

                        removeTimerQuests(timer);
                    }).build();
        }
    }
}