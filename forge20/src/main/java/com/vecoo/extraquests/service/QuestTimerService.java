package com.vecoo.extraquests.service;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.service.ExtraQuestsService;
import lombok.val;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class QuestTimerService {
    private transient final String filePath;
    private final Set<QuestTimer> questTimers;

    private transient volatile boolean dirty = false;

    public QuestTimerService(@NotNull String filePath, @NotNull MinecraftServer server) {
        this.filePath = UtilWorld.resolveWorldDirectory(filePath, server);

        this.questTimers = new HashSet<>();
    }

    @NotNull
    public Set<QuestTimer> getQuestTimers() {
        return this.questTimers;
    }

    public boolean addQuestTimer(@NotNull QuestTimer questTimer) {
        if (!this.questTimers.add(questTimer)) {
            ExtraQuests.getLogger().error("Failed to add quest timer {}.", questTimer.questID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public boolean removeQuestTimer(@NotNull QuestTimer questTimer) {
        if (!this.questTimers.remove(questTimer)) {
            ExtraQuests.getLogger().error("Failed to remove quest timer {}.", questTimer.questID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public void save() {
        UtilGson.writeFileAsync(this.filePath, "quest_timers.json", UtilGson.getGson().toJson(this)).join();
    }

    private void saveInternal() {
        TaskTimer.builder()
                .withoutDelay()
                .interval(150 * 20L)
                .infinite()
                .consume(task -> {
                    if (ExtraQuests.getInstance().getServer().isRunning() && this.dirty) {
                        UtilGson.writeFileAsync(this.filePath, "quest_timers.json",
                                UtilGson.getGson().toJson(this)).thenRun(() -> this.dirty = false);
                    }
                })
                .build();
    }

    public void init() {
        this.questTimers.clear();

        UtilGson.readFileAsync(this.filePath, "quest_timers.json", el -> {
            val questTimerProvider = UtilGson.getGson().fromJson(el, QuestTimerService.class);
            val time = System.currentTimeMillis();

            for (QuestTimer questTimer : questTimerProvider.getQuestTimers()) {
                if (questTimer.endTime() > time) {
                    this.questTimers.add(questTimer);
                    ExtraQuestsService.startQuestTimer(questTimer);
                } else {
                    ExtraQuestsService.questReset(questTimer);
                }
            }
        }).join();

        save();
        saveInternal();
    }
}