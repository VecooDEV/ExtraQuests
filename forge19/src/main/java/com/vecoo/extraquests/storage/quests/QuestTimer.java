package com.vecoo.extraquests.storage.quests;

import java.util.Date;
import java.util.UUID;

public class QuestTimer {
    private final UUID playerUUID;
    private final String quest;
    private final long endTime;

    public QuestTimer(UUID playerUUID, String quest, long time) {
        this.playerUUID = playerUUID;
        this.quest = quest;
        this.endTime = new Date().getTime() + (time * 1000L);
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getQuest() {
        return this.quest;
    }

    public long getEndTime() {
        return this.endTime;
    }
}