package com.vecoo.extraquests.config;

import com.google.common.collect.Sets;
import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

import java.util.Set;

public class ServerConfig {
    private boolean blacklistConsole = false;
    private Set<String> blacklistConsoleList = Sets.newHashSet("op", "gamemode");

    public boolean isBlacklistConsole() {
        return this.blacklistConsole;
    }

    public Set<String> getBlacklistConsoleList() {
        return this.blacklistConsoleList;
    }

    private void write() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "config.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        boolean completed = UtilGson.readFileAsync("/config/ExtraQuests/", "config.json", el -> {
            ServerConfig config = UtilGson.newGson().fromJson(el, ServerConfig.class);

            this.blacklistConsole = config.isBlacklistConsole();
            this.blacklistConsoleList = config.getBlacklistConsoleList();
        }).join();

        if (!completed) {
            ExtraQuests.getLogger().error("[ExtraQuests] Failed init server config.");
            write();
        }
    }
}