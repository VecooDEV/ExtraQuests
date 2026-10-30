package com.vecoo.extraquests.listener;

import com.vecoo.extraquests.runtime.QuestFeatures;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class ExtraQuestsListener {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                QuestFeatures.failChallenges(player, "death");
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (entity instanceof ServerPlayer player && damageTaken > 0F && !blocked) {
                QuestFeatures.failChallenges(player, "damage");
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> QuestFeatures.logout(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(QuestFeatures::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> QuestFeatures.stopped());
    }
}
