package com.vecoo.extraquests.timer;

import java.util.UUID;

public interface Listing {
    UUID getPlayerUUID();

    String getQuestID();

    long getEndTime();
}