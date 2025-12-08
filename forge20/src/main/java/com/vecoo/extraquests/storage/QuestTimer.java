package com.vecoo.extraquests.storage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class QuestTimer {
    private final UUID playerUUID;
    private final String questID;
    private final long endTime;

    public QuestTimer(@NotNull UUID playerUUID, @NotNull String questID, int secondsTimer) {
        this.playerUUID = playerUUID;
        this.questID = questID;
        this.endTime = System.currentTimeMillis() + (secondsTimer * 1000L);
    }

    @NotNull
    public UUID playerUUID() {
        return this.playerUUID;
    }

    @NotNull
    public String questID() {
        return this.questID;
    }

    public long endTime() {
        return this.endTime;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        QuestTimer timerStorage = (QuestTimer) object;

        return Objects.equals(this.playerUUID, timerStorage.playerUUID) && Objects.equals(this.questID, timerStorage.questID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.playerUUID, this.questID);
    }
}