package com.vecoo.extraquests.listener;

import com.vecoo.extraquests.runtime.QuestFeatures;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class ExtraQuestsListener {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(@NotNull LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestFeatures.failChallenges(player, "death");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamagePost(@NotNull LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0F) {
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
    public void onServerTickPost(@NotNull TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            QuestFeatures.tick(ServerLifecycleHooks.getCurrentServer());
        }
    }
}
