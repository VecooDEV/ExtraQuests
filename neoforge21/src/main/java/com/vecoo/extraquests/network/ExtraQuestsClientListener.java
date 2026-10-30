package com.vecoo.extraquests.network;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.NotNull;

public class ExtraQuestsClientListener {
    @SubscribeEvent
    public void onClientPlayerNetworkLoggingOut(@NotNull ClientPlayerNetworkEvent.LoggingOut event) {
        QuestClientState.update(new CompoundTag());
    }
}
