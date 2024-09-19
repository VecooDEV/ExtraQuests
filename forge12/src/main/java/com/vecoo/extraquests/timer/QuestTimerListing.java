package com.vecoo.extraquests.timer;

import java.util.Date;
import java.util.UUID;

public class QuestTimerListing {
    private final UUID playerUUID;

    private final String questID;

    private final long endTime;

    public QuestTimerListing(UUID playerUUID, String quest, long time) {
        this.playerUUID = playerUUID;
        this.questID = quest;
        this.endTime = new Date().getTime() + (time * 1000L);
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
}