package com.vecoo.extraquests.api.quests;

import com.vecoo.extraquests.api.factory.QuestsFactory;
import com.vecoo.extraquests.storage.quests.QuestTimer;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;

public class QuestReset {
    public static boolean questReset(QuestTimer questTimer, boolean isTimer) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Quest quest = file.getQuest(file.getID(questTimer.getQuestID()));

        if (quest == null) {
            return false;
        }

        if (!QuestsFactory.removeQuestTimer(questTimer) && isTimer) {
            return false;
        }

        TeamData teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(questTimer.getPlayerUUID()).map(file::getOrCreateTeamData).orElse(file.getOrCreateTeamData(questTimer.getPlayerUUID()));

        quest.forceProgress(teamData, new ProgressChange(file, quest, questTimer.getPlayerUUID()).setReset(true));
        return true;
    }
}
