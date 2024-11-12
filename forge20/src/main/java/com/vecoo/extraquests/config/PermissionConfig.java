package com.vecoo.extraquests.config;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extraquests.ExtraQuests;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class PermissionConfig {
    private HashMap<String, Integer> permissionCommands;

    public PermissionConfig() {
        this.permissionCommands = new HashMap<>();
        this.permissionCommands.put("minecraft.command.extraquests", 2);
    }

    public HashMap<String, Integer> getPermissionCommand() {
        return this.permissionCommands;
    }

    private void write() {
        UtilGson.writeFileAsync("/config/ExtraQuests/", "permission.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        try {
            CompletableFuture<Boolean> future = UtilGson.readFileAsync("/config/ExtraQuests/", "permission.json", el -> this.permissionCommands = UtilGson.newGson().fromJson(el, PermissionConfig.class).getPermissionCommand());
            if (!future.join()) {
                write();
            }
        } catch (Exception e) {
            ExtraQuests.getLogger().error("[ExtraQuests] Error in permission config.");
            write();
        }
    }
}