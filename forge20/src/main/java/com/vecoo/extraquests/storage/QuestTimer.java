package com.vecoo.extraquests.storage;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record QuestTimer(@NotNull UUID playerUUID, @NotNull String questID, long endTime) {
    public QuestTimer(@NotNull UUID playerUUID, @NotNull String questID, int secondsTimer) {
        this(playerUUID, questID, System.currentTimeMillis() + (secondsTimer * 1000L));
    }
}