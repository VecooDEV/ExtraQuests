package com.vecoo.extraquests.api.factory;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.storage.QuestTimer;
import com.vecoo.extraquests.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ExtraQuestsFactory {
    public static class QuestTimerProvider {
        @NotNull
        public static Set<QuestTimer> storage() {
            return ExtraQuests.instance().questTimerProvider().storage();
        }

        public static boolean add(@NotNull UUID playerUUID, @NotNull String questID, int seconds) {
            QuestTimer timer = new QuestTimer(playerUUID, questID, seconds);

            if (!ExtraQuests.instance().questTimerProvider().add(timer)) {
                return false;
            }

            Utils.startQuestTimer(timer);
            return true;
        }

        public static boolean remove(@NotNull QuestTimer questTimer) {
            return ExtraQuests.instance().questTimerProvider().remove(questTimer);
        }
    }
}