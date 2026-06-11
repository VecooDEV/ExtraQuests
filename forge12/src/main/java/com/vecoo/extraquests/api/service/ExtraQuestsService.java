package com.vecoo.extraquests.api.service;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.manager.ExtraQuestsManager;
import com.vecoo.extraquests.service.PlayerStorage;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class ExtraQuestsService {
    @Nonnull
    public static Map<UUID, PlayerStorage> getStorage() {
        return ExtraQuests.getInstance().getPlayerService().getStorage();
    }

    @Nonnull
    public static Map<String, Long> getQuestTimers(@Nonnull UUID playerUUID) {
        return ExtraQuests.getInstance().getPlayerService().getStorage(playerUUID).getQuestTimers();
    }

    public static void addQuestTimer(@Nonnull UUID playerUUID, @Nonnull String questID, int seconds) {
        val endTime = System.currentTimeMillis() + (seconds * 1000L);

        ExtraQuests.getInstance().getPlayerService().modifyStorage(playerUUID, storage -> storage.addQuestTimer(questID, endTime));
        ExtraQuestsManager.startQuestTimer(playerUUID, questID, endTime);
    }

    public static void removeQuestTimer(@Nonnull UUID playerUUID, @Nonnull String questID) {
        ExtraQuests.getInstance().getPlayerService().modifyStorage(playerUUID, storage -> storage.removeQuestTimer(questID));
    }
}