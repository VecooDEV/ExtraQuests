package com.vecoo.extraquests.config;

import com.google.common.collect.Sets;
import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.ConfigSerializable;
import lombok.Getter;

import java.util.Set;

@Getter
@ConfigSerializable
public class ServerConfig {
    private boolean blacklistConsole = false;
    private Set<String> blacklistConsoleList = Sets.newHashSet("op", "gamemode");
}