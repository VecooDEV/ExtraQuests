package com.vecoo.extraquests.storage.quests;

import java.util.Date;
import java.util.UUID;

public class QuestTimer {
    private final UUID playerUUID;
    private final String questID;
    private final long endTime;

    public QuestTimer(UUID playerUUID, String questID, long time) {
        this.playerUUID = playerUUID;
        this.questID = questID;
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