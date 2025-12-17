package com.vecoo.extraquests.config;

import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.ConfigSerializable;
import lombok.Getter;

@Getter
@ConfigSerializable
public class LocaleConfig {
    private String reload = "&e(!) Configs have been reloaded.";
    private String addKeyValue = "&e(!) You have added a key %key% value %value% to the player %player%.";

    private String errorReload = "&c(!) Reload error, checking console and fixes config.";
    private String playerNotFound = "&c(!) Player %player% not found.";
}