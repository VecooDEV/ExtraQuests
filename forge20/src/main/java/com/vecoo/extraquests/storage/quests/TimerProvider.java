package com.vecoo.extraquests.storage.quests;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
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

    public void addTimer(TimerStorage timer) {
        if (!this.timers.add(timer)) {
            ExtraQuests.getLogger().error("[ExtraQuests] Failed to add timer " + timer.toString());
            return;
        }

        write();
    }

    public void removeTimer(TimerStorage timer) {
        if (!this.timers.remove(timer)) {
            ExtraQuests.getLogger().error("[ExtraQuests] Failed to remove timer " + timer.toString());
            return;
        }

        write();
    }

    private void write() {
        UtilGson.writeFileAsync(filePath, "TimerStorage.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        CompletableFuture<Boolean> future = UtilGson.readFileAsync(filePath, "TimerStorage.json", el -> {
            long time = System.currentTimeMillis();

            for (TimerStorage timer : UtilGson.newGson().fromJson(el, TimerProvider.class).getTimers()) {
                if (timer.getEndTime() > time) {
                    this.timers.add(timer);
                    ExtraQuestsFactory.TimerProvider.startTimer(timer);
                } else {
                    Utils.questReset(timer);
                }
            }

            write();
        });
        if (!future.join()) {
            write();
        }
    }
}
