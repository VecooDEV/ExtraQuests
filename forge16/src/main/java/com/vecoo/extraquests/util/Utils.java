package com.vecoo.extraquests.util;

import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
import com.vecoo.extraquests.storage.TimerStorage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;

import javax.annotation.Nonnull;

public class Utils {
    public static boolean questReset(@Nonnull TimerStorage timer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(timer.getQuestID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for " + timer.getQuestID());
            ExtraQuestsFactory.TimerProvider.removeTimerQuests(timer);
            return false;
        }

        ProgressChange progressChange = new ProgressChange(file);
        progressChange.origin = quest;
        progressChange.player = timer.getPlayerUUID();

        quest.forceProgress(file.getData(FTBTeamsAPI.getPlayerTeamID(timer.getPlayerUUID())), progressChange);
        return true;
    }

    public static void startTimer(@Nonnull TimerStorage timer) {
        TaskTimer.builder()
                .delay((timer.getEndTime() - System.currentTimeMillis()) / 50L)
                .consume(task -> {
                    if (!Utils.questReset(timer)) {
                        task.cancel();
                        return;
                    }

                    ExtraQuestsFactory.TimerProvider.removeTimerQuests(timer);
                }).build();
    }
}
