package com.vecoo.extraquests.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import com.vecoo.extraquests.timer.QuestTimer;
import com.vecoo.extraquests.timer.QuestTimerFactory;

import java.util.Collections;

public class Utils {
    public static void timerExpired(QuestTimer questTimer) {
        QuestObject quest = ServerQuestFile.INSTANCE.get(ServerQuestFile.INSTANCE.getID(questTimer.getQuest()));

        if (quest == null) {
            return;
        }

        ForgePlayer forgePlayer = Universe.get().getPlayer(questTimer.getPlayerUUID());

        if (forgePlayer == null) {
            return;
        }

        for (ForgeTeam team : Collections.singleton(forgePlayer.team)) {
            quest.forceProgress(ServerQuestData.get(team), ChangeProgress.RESET, true);
        }

        QuestTimerFactory.removeQuestTimer(questTimer);
    }
}