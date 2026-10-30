package com.vecoo.extraquests;

import com.vecoo.extraquests.network.QuestClientState;
import com.vecoo.extraquests.network.QuestStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;

public class ExtraQuestsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(QuestStatePayload.TYPE, (payload, context) -> {
            if (context.client().getConnection() != null && context.client().getConnection().getConnection().isConnected()) {
                QuestClientState.update(payload.data());
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> QuestClientState.update(new CompoundTag()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> QuestClientState.update(new CompoundTag()));
    }
}
