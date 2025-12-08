package com.vecoo.extraquests.storage;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.Utils;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class QuestTimerProvider {
    private transient final String filePath;
    private final Set<QuestTimer> questTimers;

    private transient boolean intervalStarted = false;
    private transient volatile boolean dirty = false;

    public QuestTimerProvider(@NotNull String filePath, @NotNull MinecraftServer server) {
        this.filePath = UtilWorld.resolveWorldDirectory(filePath, server);

        this.questTimers = new HashSet<>();
    }

    @NotNull
    public Set<QuestTimer> storage() {
        return this.questTimers;
    }

    public boolean add(@NotNull QuestTimer questTimer) {
        if (!this.questTimers.add(questTimer)) {
            ExtraQuests.logger().error("Failed to add timer {}.", questTimer.questID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public boolean remove(@NotNull QuestTimer questTimer) {
        if (!this.questTimers.remove(questTimer)) {
            ExtraQuests.logger().error("Failed to remove timer {}.", questTimer.questID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public void write() {
        UtilGson.writeFileAsync(this.filePath, "Timers.json", UtilGson.gson().toJson(this)).join();
    }

    private void writeInterval() {
        if (!this.intervalStarted) {
            TaskTimer.builder()
                    .withoutDelay()
                    .interval(30 * 20L)
                    .infinite()
                    .consume(task -> {
                        if (ExtraQuests.instance().server().isRunning() && this.dirty) {
                            UtilGson.writeFileAsync(this.filePath, "Timers.json",
                                    UtilGson.gson().toJson(this)).thenRun(() -> this.dirty = false);
                        }
                    })
                    .build();

            this.intervalStarted = true;
        }
    }

    public void init() {
        this.questTimers.clear();

        UtilGson.readFileAsync(this.filePath, "Timers.json", el -> {
            QuestTimerProvider provider = UtilGson.gson().fromJson(el, QuestTimerProvider.class);
            long time = System.currentTimeMillis();

            for (QuestTimer questTimer : provider.storage()) {
                if (questTimer.endTime() > time) {
                    this.questTimers.add(questTimer);
                    Utils.startQuestTimer(questTimer);
                } else {
                    Utils.questReset(questTimer);
                }
            }
        }).join();

        write();
        writeInterval();
    }
}