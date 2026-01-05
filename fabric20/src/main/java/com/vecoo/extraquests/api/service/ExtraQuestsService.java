package com.vecoo.extraquests.api.service;

import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.service.QuestTimer;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ExtraQuestsService {
    @NotNull
    public static Set<QuestTimer> getQuestTimers() {
        return ExtraQuests.getInstance().getQuestTimerService().getQuestTimers();
    }

    public static boolean addQuestTimer(@NotNull UUID playerUUID, @NotNull String questID, int seconds) {
        val timer = new QuestTimer(playerUUID, questID, seconds);

        if (!ExtraQuests.getInstance().getQuestTimerService().addQuestTimer(timer)) {
            return false;
        }

        startQuestTimer(timer);
        return true;
    }

    public static boolean removeQuestTimer(@NotNull QuestTimer questTimer) {
        return ExtraQuests.getInstance().getQuestTimerService().removeQuestTimer(questTimer);
    }

    public static boolean questReset(@NotNull QuestTimer questTimer) {
        val file = ServerQuestFile.INSTANCE;
        val quest = file.getQuest(file.getID(questTimer.questID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for {}.", questTimer.questID());
            ExtraQuestsService.removeQuestTimer(questTimer);
            return false;
        }

        val teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(questTimer.playerUUID())
                .map(file::getOrCreateTeamData).orElse(file.getOrCreateTeamData(questTimer.playerUUID()));

        quest.forceProgress(teamData, new ProgressChange(file, quest, questTimer.playerUUID()).setReset(true));
        return true;
    }

    public static void startQuestTimer(@NotNull QuestTimer questTimer) {
        TaskTimer.builder()
                .delay((questTimer.endTime() - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!questReset(questTimer)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsService.removeQuestTimer(questTimer);
                }).build();
    }
}