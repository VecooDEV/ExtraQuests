package com.vecoo.extraquests.config;

import com.google.common.collect.Sets;
import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.ConfigSerializable;
import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.meta.Comment;
import lombok.Getter;

import java.util.Set;

@Getter
@ConfigSerializable
@SuppressWarnings("FieldCanBeLocal")
public class ServerConfig {
    @Comment("Will there be a blacklist of console commands to execute from the command reward?")
    private boolean blacklistConsole = false;
    @Comment("All commands that will be banned.")
    private Set<String> blacklistConsoleList = Sets.newHashSet("op", "gamemode");
}