package com.vecoo.extraquests.timer;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import com.vecoo.extraquests.ExtraQuests;

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
        QuestObject quest = ServerQuestFile.INSTANCE.get(ServerQuestFile.INSTANCE.getID(listing.getQuestID()));

        if (quest == null) {
            return;
        }

        if (Universe.get().getPlayer(listing.getPlayerUUID()) == null) {
            return;
        }

        Collection<ForgeTeam> teams = Collections.singleton(Universe.get().getPlayer(listing.getPlayerUUID()).team);

        for (ForgeTeam team : teams) {
            quest.forceProgress(ServerQuestData.get(team), ChangeProgress.RESET, true);
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