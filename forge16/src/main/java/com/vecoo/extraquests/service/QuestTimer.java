package com.vecoo.extraquests.service;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = {"playerUUID", "questID"})
public class QuestTimer {
    @Nonnull
    private final UUID playerUUID;
    @Nonnull
    private final String questID;
    private final long endTime;

    public QuestTimer(@Nonnull UUID playerUUID, @Nonnull String questID, int secondsTimer) {
        this.playerUUID = playerUUID;
        this.questID = questID;
        this.endTime = System.currentTimeMillis() + (secondsTimer * 1000L);
    }
}