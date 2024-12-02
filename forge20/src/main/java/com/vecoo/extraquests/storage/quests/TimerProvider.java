package com.vecoo.extraquests.storage.quests;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.QuestsFactory;
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
                    if (!Utils.questReset(questTimer, true)) {
                        ExtraQuests.getLogger().error("[ExtraQuests] The quest or timer is invalid. Quest ID: " + questTimer.getQuestID() + ". If you deleted the quest, ignore this.");
                        QuestsFactory.removeQuestTimer(questTimer);
                    }
                }
            }, timeDiff);
            this.timers.put(questTimer, timer);
        } else {
            if (!Utils.questReset(questTimer, true)) {
                ExtraQuests.getLogger().error("[ExtraQuests] The quest or timer is invalid. Quest ID: " + questTimer.getQuestID() + ". If you deleted the quest, ignore this.");
                QuestsFactory.removeQuestTimer(questTimer);
            }
        }
    }

    public boolean removeTimer(QuestTimer questTimer) {
        Timer timer = this.timers.remove(questTimer);

        if (timer == null) {
            return false;
        }

        timer.cancel();
        return true;
    }

    public void removeAllTimers() {
        for (QuestTimer questTimer : getTimers()) {
            removeTimer(questTimer);
        }
    }
}