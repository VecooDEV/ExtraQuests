package com.vecoo.extraquests.util;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
import com.vecoo.extraquests.storage.quests.TimerStorage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;

public class Utils {
    public static boolean questReset(TimerStorage timer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(timer.getQuestID()));

        if (quest == null) {
            ExtraQuests.getLogger().error("[ExtraQuests] No quest found for " + timer.getQuestID());
            ExtraQuestsFactory.TimerProvider.removeTimerQuests(timer);
            return false;
        }

        ProgressChange progressChange = new ProgressChange(file);
        progressChange.origin = quest;
        progressChange.player = timer.getPlayerUUID();

        quest.forceProgress(file.getData(FTBTeamsAPI.getPlayerTeamID(timer.getPlayerUUID())), progressChange);
        return true;
    }
}
