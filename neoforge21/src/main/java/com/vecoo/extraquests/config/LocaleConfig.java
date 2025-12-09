package com.vecoo.extraquests.config;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

public class LocaleConfig {
    private String reload = "&e(!) Configs have been reloaded.";
    private String addKeyValue = "&e(!) You have added a key %key% value %value% to the player %player%.";

    private String playerNotFound = "&c(!) Player %player% not found.";

    public String getReload() {
        return this.reload;
    }

    public String getAddKeyValue() {
        return this.addKeyValue;
    }

    public String getPlayerNotFound() {
        return this.playerNotFound;
    }

    private void save() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "locale.json", UtilGson.getGson().toJson(this)).join();
    }

    public void init() {
        boolean completed = UtilGson.readFileAsync("/config/ExtraQuests/", "locale.json", el -> {
            LocaleConfig config = UtilGson.getGson().fromJson(el, LocaleConfig.class);

            this.reload = config.getReload();
            this.addKeyValue = config.getAddKeyValue();
            this.playerNotFound = config.getPlayerNotFound();
        }).join();

        if (!completed) {
            ExtraQuests.getLogger().error("Error init locale config, generating new locale config.");
            save();
        }
    }
}