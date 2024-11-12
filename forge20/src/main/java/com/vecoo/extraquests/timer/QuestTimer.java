package com.vecoo.extraquests.timer;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

import java.util.Date;
import java.util.UUID;

public class QuestTimer {
    private final UUID playerUUID;
    private final long questID;
    private final long endTime;

    public QuestTimer(UUID playerUUID, QuestObjectBase questObject, long time) {
        this.playerUUID = playerUUID;
        this.questID = questObject.id;
        this.endTime = new Date().getTime() + (time * 1000L);
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public long getQuestID() {
        return this.questID;
    }

    public long getEndTime() {
        return this.endTime;
    }
}