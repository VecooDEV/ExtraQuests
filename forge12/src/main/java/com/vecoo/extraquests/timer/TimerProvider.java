package com.vecoo.extraquests.timer;

import com.vecoo.extraquests.ExtraQuests;
import net.minecraftforge.common.UsernameCache;

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
        ExtraQuests.getServer().getCommandManager().executeCommand(ExtraQuests.getServer(), "ftbquests change_progress reset " + UsernameCache.getLastKnownUsername(listing.getPlayerUUID()) + " " + listing.getQuestID());
        ExtraQuests.getListingsProvider().removeListing(listing);
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