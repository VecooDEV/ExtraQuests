package com.vecoo.extraquests.util;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.timer.QuestTimerListing;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;

import java.util.*;

public class TimerProvider {
    private HashMap<QuestTimerListing, Timer> listingMap;

    public TimerProvider() {
        listingMap = new HashMap<>();
    }

    public ArrayList<QuestTimerListing> getTimers() {
        ArrayList<QuestTimerListing> keys = new ArrayList<>(listingMap.keySet());
        return keys;
    }

    public void questTimer(QuestTimerListing listing) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file.getQuest(listing.getQuestID()) != null) {
            QuestObjectBase questObject = file.getQuest(listing.getQuestID());
            ProgressChange progressChange = new ProgressChange(file, questObject, listing.getPlayerUUID()).setReset(true);
            questObject.forceProgress(ServerQuestFile.INSTANCE.getNullableTeamData(listing.getPlayerUUID()), progressChange);
        }
        ExtraQuests.getInstance().getListingsProvider().removeListing(listing);
    }

    public void addQuestTimer(QuestTimerListing listing) {
        long timeDiff = listing.getEndTime() - new Date().getTime();

        if (timeDiff > 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    questTimer(listing);
                }
            }, timeDiff);
            listingMap.put(listing, timer);
        } else {
            questTimer(listing);
        }
    }

    public void deleteQuestTimer(QuestTimerListing listing) {
        Timer timer = listingMap.remove(listing);
        if (timer != null) {
            timer.cancel();
        }
    }
}