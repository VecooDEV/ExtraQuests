package com.vecoo.extraquests.config;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

public class LocaleConfig {
    private String reload = "&e(!) Configs have been reloaded.";
    private String addKeyValue = "&e(!) You have added a key %key% value %value% to the player %player%.";

    private String playerNotFound = "&c(!) Player %player% not found.";

    public String reload() {
        return this.reload;
    }

    public String addKeyValue() {
        return this.addKeyValue;
    }

    public String playerNotFound() {
        return this.playerNotFound;
    }

    private void write() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "locale.json", UtilGson.gson().toJson(this)).join();
    }

    public void init() {
        boolean completed = UtilGson.readFileAsync("/config/ExtraQuests/", "locale.json", el -> {
            LocaleConfig config = UtilGson.gson().fromJson(el, LocaleConfig.class);

            this.reload = config.reload();
            this.addKeyValue = config.addKeyValue();
            this.playerNotFound = config.playerNotFound();
        }).join();

        if (!completed) {
            ExtraQuests.logger().error("Error init locale config, generating new locale config.");
            write();
        }
    }
}