package com.vecoo.extraquests.storage.quests;

import java.util.Objects;
import java.util.UUID;

public class TimerStorage {
    private final UUID playerUUID;
    private final String questID;
    private final long endTime;

    public TimerStorage(UUID playerUUID, String questID, int secondsTimer) {
        this.playerUUID = playerUUID;
        this.questID = questID;
        this.endTime = System.currentTimeMillis() + (secondsTimer * 1000L);
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getQuestID() {
        return this.questID;
    }

    public long getEndTime() {
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

        TimerStorage timerStorage = (TimerStorage) object;

        return Objects.equals(this.playerUUID, timerStorage.playerUUID) && Objects.equals(this.questID, timerStorage.questID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.playerUUID, this.questID);
    }
}