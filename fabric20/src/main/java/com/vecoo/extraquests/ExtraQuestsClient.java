package com.vecoo.extraquests;

import com.vecoo.extraquests.network.QuestClientState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;

public class ExtraQuestsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> QuestClientState.update(new CompoundTag()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> QuestClientState.update(new CompoundTag()));
    }
}
