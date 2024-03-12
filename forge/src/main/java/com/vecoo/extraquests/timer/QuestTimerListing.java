package com.vecoo.extraquests.timer;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

import java.util.Date;
import java.util.UUID;

public class QuestTimerListing implements Listing {

    private final UUID playerUUID;

    private final long questID;

    private final long endTime;

    public QuestTimerListing(UUID playerUUID, QuestObjectBase questObject, long time) {
        this.playerUUID = playerUUID;
        this.questID = questObject.id;
        this.endTime = new Date().getTime() + (time * 60000L);
    }

    @Override
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Override
    public long getQuestID() {
        return this.questID;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }
}