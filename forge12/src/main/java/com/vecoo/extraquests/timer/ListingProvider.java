package com.vecoo.extraquests.timer;

import com.google.gson.Gson;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.GsonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListingProvider {
    private ArrayList<QuestTimerListing> listing;

    public ListingProvider() {
        listing = new ArrayList<>();
    }

    public List<QuestTimerListing> getListing() {
        return this.listing;
    }

    public void addListing(QuestTimerListing listing) {
        this.listing.add(listing);
        ExtraQuests.getTimerProvider().addQuestTimer(listing);
       writeToFile();
    }

    public void removeListing(QuestTimerListing listing) {
        this.listing.remove(listing);
        ExtraQuests.getTimerProvider().deleteQuestTimer(listing);
        writeToFile();
    }

    private void writeToFile() {
        Gson gson = GsonUtils.newGson();
        CompletableFuture<Boolean> future = GsonUtils.writeFileAsync("/config/temp/ExtraQuests/timer/", "listings.json", gson.toJson(this));
        future.join();
    }

    public void init() {
        CompletableFuture<Boolean> future = GsonUtils.readFileAsync("/config/temp/ExtraQuests/timer/", "listings.json", el -> {
            Gson gson = GsonUtils.newGson();
            ListingProvider data = gson.fromJson(el, ListingProvider.class);

            for (QuestTimerListing listing : data.getListing()) {
                if (listing.getEndTime() > new Date().getTime()) {
                    this.listing.add(listing);
                    ExtraQuests.getTimerProvider().addQuestTimer(listing);
                } else {
                    ExtraQuests.getTimerProvider().questTimer(listing);
                }
            }
        });
        if (!future.join()) {
            writeToFile();
        }
    }
}