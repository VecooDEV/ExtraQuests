package com.vecoo.extraquests.listener;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.task.KeyValueTask;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RegisterFTB {

    @SubscribeEvent
    public void registerTasks(RegistryEvent.Register<TaskType> event) {
        event.getRegistry().registerAll(
                KeyValueTask.TYPE = new TaskType(KeyValueTask::new).setRegistryName("key_value").setIcon(Icon.getIcon("minecraft:items/paper")));
        FTBQuests.PROXY.setTaskGuiProviders();
    }

    @SubscribeEvent
    public void registerRewards(RegistryEvent.Register<RewardType> event) {
        event.getRegistry().registerAll(
                KeyValueReward.TYPE = new RewardType(KeyValueReward::new).setRegistryName("key_value").setIcon(Icon.getIcon("minecraft:items/paper")),
                TimerReward.TYPE = new RewardType(TimerReward::new).setRegistryName("timer").setIcon(Icon.getIcon("minecraft:items/clock_07")));
        FTBQuests.PROXY.setRewardGuiProviders();
    }
}