package com.vecoo.extraquests.manager;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import com.vecoo.extralib.scheduler.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.service.ExtraQuestsService;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.UUID;

public class ExtraQuestsManager {
    public static void startQuestTimer(@Nonnull UUID playerUUID, String questID, long endTime) {
        TaskTimer.builder()
                .delay((endTime - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!questReset(playerUUID, questID)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsService.removeQuestTimer(playerUUID, questID);
                }).build();
    }

    public static boolean questReset(@Nonnull UUID playerUUID, @Nonnull String questID) {
        val file = ServerQuestFile.INSTANCE;
        val quest = file.getQuest(file.getID(questID));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for {}.", quest);
            ExtraQuestsService.removeQuestTimer(playerUUID, questID);
            return false;
        }

        val forgePlayer = Universe.get().getPlayer(playerUUID);

        if (forgePlayer == null) {
            return false;
        }

        for (ForgeTeam team : Collections.singleton(forgePlayer.team)) {
            quest.forceProgress(ServerQuestData.get(team), ChangeProgress.RESET, true);
        }

        return true;
    }
}
