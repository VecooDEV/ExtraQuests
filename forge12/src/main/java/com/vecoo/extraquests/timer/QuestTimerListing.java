package com.vecoo.extraquests.timer;

import java.util.Date;
import java.util.UUID;

public class QuestTimerListing implements Listing {

    private final UUID playerUUID;

    private final String questID;

    private final long endTime;

    public QuestTimerListing(UUID playerUUID, String quest, long time) {
        this.playerUUID = playerUUID;
        this.questID = quest;
        this.endTime = new Date().getTime() + (time * 60000L);
    }

    @Override
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Override
    public String getQuestID() {
        return this.questID;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }
}