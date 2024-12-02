package com.vecoo.extraquests.storage.quests;

import com.vecoo.extraquests.util.Utils;

import java.util.*;

public class TimerProvider {
    private final HashMap<QuestTimer, Timer> timers;

    public TimerProvider() {
        this.timers = new HashMap<>();
    }

    public ArrayList<QuestTimer> getTimers() {
        return new ArrayList<>(this.timers.keySet());
    }

    public void startTimer(QuestTimer questTimer) {
        long timeDiff = questTimer.getEndTime() - new Date().getTime();

        if (timeDiff > 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Utils.timerExpired(questTimer);
                }
            }, timeDiff);
            this.timers.put(questTimer, timer);
        } else {
            Utils.timerExpired(questTimer);
        }
    }

    public void removeTimer(QuestTimer questTimer) {
        this.timers.remove(questTimer).cancel();
    }

    public void removeAllTimers() {
        for (QuestTimer questTimer : getTimers()) {
            removeTimer(questTimer);
        }
    }
}
