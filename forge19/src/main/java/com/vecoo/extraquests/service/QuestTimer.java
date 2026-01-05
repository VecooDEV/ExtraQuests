package com.vecoo.extraquests.service;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public record QuestTimer(@NotNull UUID playerUUID, @NotNull String questID, long endTime) {
    public QuestTimer(@NotNull UUID playerUUID, @NotNull String questID, int secondsTimer) {
        this(playerUUID, questID, System.currentTimeMillis() + (secondsTimer * 1000L));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof QuestTimer questTimer)) {
            return false;
        }

        return this.playerUUID.equals(questTimer.playerUUID) && this.questID.equals(questTimer.questID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.playerUUID, this.questID);
    }
}