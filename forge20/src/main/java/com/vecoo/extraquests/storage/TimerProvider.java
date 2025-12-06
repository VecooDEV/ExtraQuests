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

public class TimerProvider {
    private transient final String filePath;
    private final Set<TimerStorage> timers;

    private transient boolean intervalStarted = false;
    private transient volatile boolean dirty = false;

    public TimerProvider(@NotNull String filePath, @NotNull MinecraftServer server) {
        this.filePath = UtilWorld.resolveWorldDirectory(filePath, server);

        this.timers = new HashSet<>();
    }

    @NotNull
    public Set<TimerStorage> storage() {
        return this.timers;
    }

    public boolean addTimer(@NotNull TimerStorage timerStorage) {
        if (!this.timers.add(timerStorage)) {
            ExtraQuests.logger().error("Failed to add timer {}.", timerStorage.questID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public boolean removeTimer(@NotNull TimerStorage timerStorage) {
        if (!this.timers.remove(timerStorage)) {
            ExtraQuests.logger().error("Failed to remove timer {}.", timerStorage.questID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public void write() {
        UtilGson.writeFileAsync(this.filePath, "TimerStorage.json", UtilGson.gson().toJson(this)).join();
    }

    private void writeInterval() {
        if (!this.intervalStarted) {
            TaskTimer.builder()
                    .withoutDelay()
                    .interval(30 * 20L)
                    .infinite()
                    .consume(task -> {
                        if (ExtraQuests.instance().server().isRunning() && this.dirty) {
                            UtilGson.writeFileAsync(this.filePath, "TimerStorage.json",
                                    UtilGson.gson().toJson(this)).thenRun(() -> this.dirty = false);
                        }
                    })
                    .build();

            this.intervalStarted = true;
        }
    }

    public void init() {
        this.timers.clear();

        UtilGson.readFileAsync(this.filePath, "TimerStorage.json", el -> {
            TimerProvider provider = UtilGson.gson().fromJson(el, TimerProvider.class);
            long time = System.currentTimeMillis();

            for (TimerStorage timer : provider.storage()) {
                if (timer.endTime() > time) {
                    this.timers.add(timer);
                    Utils.startQuestTimer(timer);
                } else {
                    Utils.questReset(timer);
                }
            }
        }).join();

        write();
        writeInterval();
    }
}