package com.vecoo.extraquests.timer;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuestTimerProvider {
    private final String filePath = UtilWorld.worldDirectory(ExtraQuests.getInstance().getConfig().getTimerStorage());
    private final ArrayList<QuestTimer> questTimers;

    public QuestTimerProvider() {
        this.questTimers = new ArrayList<>();
    }

    public List<QuestTimer> getQuestTimers() {
        return this.questTimers;
    }

    public void addQuestTimer(QuestTimer questTimer) {
        this.questTimers.add(questTimer);
        ExtraQuests.getInstance().getTimerProvider().startTimer(questTimer);
        write();
    }

    public void removeQuestTimer(QuestTimer questTimer) {
        this.questTimers.remove(questTimer);
        ExtraQuests.getInstance().getTimerProvider().removeTimer(questTimer);
        write();
    }

    private void write() {
        UtilGson.writeFileAsync(filePath, "timers.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        CompletableFuture<Boolean> future = UtilGson.readFileAsync(filePath, "timers.json", el -> {
            for (QuestTimer questTimer : UtilGson.newGson().fromJson(el, QuestTimerProvider.class).getQuestTimers()) {
                if (questTimer.getEndTime() > new Date().getTime()) {
                    this.questTimers.add(questTimer);
                    ExtraQuests.getInstance().getTimerProvider().startTimer(questTimer);
                } else {
                    Utils.timerExpired(questTimer);
                }
            }
        });
        if (!future.join()) {
            write();
        }
    }
}