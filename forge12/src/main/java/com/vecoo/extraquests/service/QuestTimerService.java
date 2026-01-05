package com.vecoo.extraquests.service;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.service.ExtraQuestsService;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

@Getter
public class QuestTimerService {
    @Nonnull
    private transient final String filePath;
    @Nonnull
    private final Set<QuestTimer> questTimers;

    private transient volatile boolean dirty = false;

    public QuestTimerService(@Nonnull String filePath, @Nonnull MinecraftServer server) {
        this.filePath = UtilWorld.resolveWorldDirectory(filePath, server);

        this.questTimers = new HashSet<>();
    }

    public boolean addQuestTimer(@Nonnull QuestTimer questTimer) {
        if (!this.questTimers.add(questTimer)) {
            ExtraQuests.getLogger().error("Failed to add quest timer {}.", questTimer.getQuestID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public boolean removeQuestTimer(@Nonnull QuestTimer questTimer) {
        if (!this.questTimers.remove(questTimer)) {
            ExtraQuests.getLogger().error("Failed to remove quest timer {}.", questTimer.getQuestID());
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
                    if (ExtraQuests.getInstance().getServer().isServerRunning() && this.dirty) {
                        UtilGson.writeFileAsync(this.filePath, "quest_timers.json",
                                UtilGson.getGson().toJson(this)).thenRun(() -> this.dirty = false);
                    }
                })
                .build();
    }

    public void init() {
        this.questTimers.clear();

        UtilGson.readFileAsync(this.filePath, "quest_timers.json", el -> {
            val questTimerService = UtilGson.getGson().fromJson(el, QuestTimerService.class);
            val time = System.currentTimeMillis();

            for (QuestTimer questTimer : questTimerService.getQuestTimers()) {
                if (questTimer.getEndTime() > time) {
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