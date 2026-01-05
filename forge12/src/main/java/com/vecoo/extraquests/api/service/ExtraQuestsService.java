package com.vecoo.extraquests.api.service;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.service.QuestTimer;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ExtraQuestsService {
    @Nonnull
    public static Set<QuestTimer> getQuestTimers() {
        return ExtraQuests.getInstance().getQuestTimerService().getQuestTimers();
    }

    public static boolean addQuestTimer(@Nonnull UUID playerUUID, @Nonnull String questID, int seconds) {
        val timer = new QuestTimer(playerUUID, questID, seconds);

        if (!ExtraQuests.getInstance().getQuestTimerService().addQuestTimer(timer)) {
            return false;
        }

        startQuestTimer(timer);
        return true;
    }

    public static boolean removeQuestTimer(@Nonnull QuestTimer questTimer) {
        return ExtraQuests.getInstance().getQuestTimerService().removeQuestTimer(questTimer);
    }

    public static boolean questReset(@Nonnull QuestTimer questTimer) {
        val file = ServerQuestFile.INSTANCE;
        val quest = file.getQuest(file.getID(questTimer.getQuestID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for {}.", questTimer.getQuestID());
            ExtraQuestsService.removeQuestTimer(questTimer);
            return false;
        }

        val forgePlayer = Universe.get().getPlayer(questTimer.getPlayerUUID());

        if (forgePlayer == null) {
            return false;
        }

        for (ForgeTeam team : Collections.singleton(forgePlayer.team)) {
            quest.forceProgress(ServerQuestData.get(team), ChangeProgress.RESET, true);
        }

        return true;
    }

    public static void startQuestTimer(@Nonnull QuestTimer questTimer) {
        TaskTimer.builder()
                .delay((questTimer.getEndTime() - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!questReset(questTimer)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsService.removeQuestTimer(questTimer);
                }).build();
    }
}