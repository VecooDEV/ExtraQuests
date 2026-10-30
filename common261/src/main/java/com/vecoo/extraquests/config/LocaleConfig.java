package com.vecoo.extraquests.config;

import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.ConfigSerializable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@ConfigSerializable
@SuppressWarnings("FieldMayBeFinal")
public class LocaleConfig {
    @NotNull
    private String reload = "&e(!) Configs have been reloaded.";
    @NotNull
    private String addKeyValue = "&e(!) You have added a key %key% value %value% to the player %player%.";

    @NotNull
    private String errorReload = "&c(!) Reload error, checking console and fixes config.";
    @NotNull
    private String playerNotFound = "&c(!) Player %player% not found.";
}
