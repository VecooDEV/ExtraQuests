package com.vecoo.extraquests.config;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

import java.util.concurrent.CompletableFuture;

public class LocaleConfig {
    private String reload = "&e(!) Configs have been reloaded.";

    private String addKeyValueSource = "&e(!) You have added a key %key% value %value% to the player %player%.";
    private String addKeyValueTarget = "&e(!) You have been added a value of %value% key %key%.";

    public String getReload() {
        return this.reload;
    }

    public String getAddKeyValueSource() {
        return this.addKeyValueSource;
    }

    public String getAddKeyValueTarget() {
        return this.addKeyValueTarget;
    }

    private void write() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "locale.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        try {
            CompletableFuture<Boolean> future = UtilGson.readFileAsync("/config/ExtraQuests/", "locale.json", el -> {
                LocaleConfig config = UtilGson.newGson().fromJson(el, LocaleConfig.class);

                this.reload = config.getReload();
                this.addKeyValueSource = config.getAddKeyValueSource();
                this.addKeyValueTarget = config.getAddKeyValueTarget();
            });
            if (!future.join()) {
                write();
            }
        } catch (Exception e) {
            ExtraQuests.getLogger().error("[ExtraQuests] Error in locale config.", e);
            write();
        }
    }
}