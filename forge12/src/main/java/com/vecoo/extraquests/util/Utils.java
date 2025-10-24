package com.vecoo.extraquests.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import com.vecoo.extralib.task.TaskTimer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
import com.vecoo.extraquests.storage.TimerStorage;

import javax.annotation.Nonnull;
import java.util.Collections;

public class Utils {
    public static boolean questReset(@Nonnull TimerStorage timer) {
        QuestObject quest = ServerQuestFile.INSTANCE.get(ServerQuestFile.INSTANCE.getID(timer.getQuestID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("No quest found for " + timer.getQuestID());
            ExtraQuestsFactory.TimerProvider.removeTimerQuests(timer);
            return false;
        }

        ForgePlayer forgePlayer = Universe.get().getPlayer(timer.getPlayerUUID());

        if (forgePlayer == null) {
            return false;
        }

        for (ForgeTeam team : Collections.singleton(forgePlayer.team)) {
            quest.forceProgress(ServerQuestData.get(team), ChangeProgress.RESET, true);
        }
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
