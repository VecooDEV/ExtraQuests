package com.vecoo.extraquests.config;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerConfig {
    private boolean blacklistConsole = false;
    private List<String> blacklistConsoleList = Arrays.asList("op", "gamemode");

    public boolean isBlacklistConsole() {
        return this.blacklistConsole;
    }

    public List<String> getBlacklistConsoleList() {
        return this.blacklistConsoleList;
    }

    private void write() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "config.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        try {
            CompletableFuture<Boolean> future = UtilGson.readFileAsync("/config/ExtraQuests/", "config.json", el -> {
                ServerConfig config = UtilGson.newGson().fromJson(el, ServerConfig.class);

                this.blacklistConsole = config.isBlacklistConsole();
                this.blacklistConsoleList = config.getBlacklistConsoleList();
            });
            if (!future.join()) {
                write();
            }
        } catch (Exception e) {
            ExtraQuests.getLogger().error("[ExtraQuests] Error in config.", e);
            write();
        }
    }
}