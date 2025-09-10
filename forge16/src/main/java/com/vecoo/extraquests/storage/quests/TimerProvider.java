package com.vecoo.extraquests.storage.quests;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.Utils;
import net.minecraft.server.MinecraftServer;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TimerProvider {
    private transient final String filePath;
    private final Set<TimerStorage> timers;

    public TimerProvider(String filePath, MinecraftServer server) {
        this.filePath = UtilWorld.worldDirectory(filePath, server);

        this.timers = ConcurrentHashMap.newKeySet();
    }

    public Set<TimerStorage> getTimers() {
        return this.timers;
    }

    public boolean addTimer(TimerStorage timer) {
        if (!this.timers.add(timer)) {
            ExtraQuests.getLogger().error("[ExtraQuests] Failed to add timer " + timer.getQuestID());
            return false;
        }

        write().thenAccept(success -> {
            if (!success) {
                ExtraQuests.getLogger().error("[ExtraQuests] Failed to write TimerStorage.");
            }
        });
        return true;
    }

    public boolean removeTimer(TimerStorage timer) {
        if (!this.timers.remove(timer)) {
            ExtraQuests.getLogger().error("[ExtraQuests] Failed to remove timer " + timer.getQuestID());
            return false;
        }

        write().thenAccept(success -> {
            if (!success) {
                ExtraQuests.getLogger().error("[ExtraQuests] Failed to write TimerStorage.");
            }
        });
        return true;
    }

    private CompletableFuture<Boolean> write() {
        return UtilGson.writeFileAsync(this.filePath, "TimerStorage.json", UtilGson.newGson().toJson(this));
    }

    public void init() {
        UtilGson.readFileAsync(this.filePath, "TimerStorage.json", el -> {
            TimerProvider provider = UtilGson.newGson().fromJson(el, TimerProvider.class);
            long time = System.currentTimeMillis();

            for (TimerStorage timer : provider.getTimers()) {
                if (timer.getEndTime() > time) {
                    this.timers.add(timer);
                    Utils.startTimer(timer);
                } else {
                    Utils.questReset(timer);
                }
            }
        }).join();

        write().join();
    }
}
