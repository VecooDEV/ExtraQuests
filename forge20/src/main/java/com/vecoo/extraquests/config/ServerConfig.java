package com.vecoo.extraquests.config;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

import java.util.concurrent.CompletableFuture;

public class ServerConfig {
    private String timerStorage = "/%directory%/storage/ExtraQuests/";

    public String getTimerStorage() {
        return this.timerStorage;
    }

    private void write() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "config.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        try {
            CompletableFuture<Boolean> future = UtilGson.readFileAsync("/config/ExtraQuests/", "config.json", el -> {
                ServerConfig config = UtilGson.newGson().fromJson(el, ServerConfig.class);

                this.timerStorage = config.getTimerStorage();
            });
            if (!future.join()) {
                write();
            }
        } catch (Exception e) {
            ExtraQuests.getLogger().error("[ExtraQuests] Error in config.");
            write();
        }
    }
}