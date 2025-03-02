package com.vecoo.extraquests.listener;

import com.vecoo.extraquests.task.ItemUseTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TaskListener {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerInteractRightItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        for (ItemUseTask task : ServerQuestFile.INSTANCE.collect(ItemUseTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getOrCreateTeamData(event.getEntity()), event.getItemStack());
        }
    }
}
