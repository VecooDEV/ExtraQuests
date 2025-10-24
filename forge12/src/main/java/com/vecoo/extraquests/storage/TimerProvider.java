package com.vecoo.extraquests.storage;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.Utils;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class TimerProvider {
    private transient final String filePath;
    private final Set<TimerStorage> timers;

    private transient boolean intervalStarted = false;
    private transient volatile boolean dirty = false;

    public TimerProvider(@Nonnull String filePath, @Nonnull MinecraftServer server) {
        this.filePath = UtilWorld.worldDirectory(filePath, server);

        this.timers = new HashSet<>();
    }

    @Nonnull
    public Set<TimerStorage> getStorage() {
        return this.timers;
    }

    public boolean addTimer(@Nonnull TimerStorage timer) {
        if (!this.timers.add(timer)) {
            ExtraQuests.getLogger().error("Failed to add timer " + timer.getQuestID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public boolean removeTimer(@Nonnull TimerStorage timer) {
        if (!this.timers.remove(timer)) {
            ExtraQuests.getLogger().error("Failed to remove timer " + timer.getQuestID());
            return false;
        }

        this.dirty = true;
        return true;
    }

    public void write() {
        UtilGson.writeFileAsync(this.filePath, "TimerStorage.json", UtilGson.newGson().toJson(this)).join();
    }

    private void writeInterval() {
        if (!this.intervalStarted) {
            TaskTimer.builder()
                    .withoutDelay()
                    .interval(30 * 20L)
                    .infinite()
                    .consume(task -> {
                        if (ExtraQuests.getInstance().getServer().isServerRunning() && this.dirty) {
                            UtilGson.writeFileAsync(this.filePath, "TimerStorage.json",
                                    UtilGson.newGson().toJson(this)).thenRun(() -> this.dirty = false);
                        }
                    })
                    .build();

            this.intervalStarted = true;
        }
    }

    public void init() {
        UtilGson.readFileAsync(this.filePath, "TimerStorage.json", el -> {
            TimerProvider provider = UtilGson.newGson().fromJson(el, TimerProvider.class);
            long time = System.currentTimeMillis();

            for (TimerStorage timer : provider.getStorage()) {
                if (timer.getEndTime() > time) {
                    this.timers.add(timer);
                    Utils.startTimer(timer);
                } else {
                    Utils.questReset(timer);
                }
            }
        }).join();

        write();
        writeInterval();
    }
}