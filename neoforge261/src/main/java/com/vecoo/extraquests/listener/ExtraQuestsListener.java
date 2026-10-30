package com.vecoo.extraquests.listener;

import com.vecoo.extraquests.runtime.QuestFeatures;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

public class ExtraQuestsListener {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(@NotNull LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestFeatures.failChallenges(player, "death");
        }
    }

    @SubscribeEvent
    public void onLivingDamagePost(@NotNull LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getHealthDamage() > 0F) {
            QuestFeatures.failChallenges(player, "damage");
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(@NotNull PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestFeatures.logout(player);
        }
    }

    @SubscribeEvent
    public void onServerTickPost(@NotNull ServerTickEvent.Post event) {
        QuestFeatures.tick(event.getServer());
    }
}
