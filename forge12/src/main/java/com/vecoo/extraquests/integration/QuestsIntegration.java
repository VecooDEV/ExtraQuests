package com.vecoo.extraquests.integration;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.task.KeyValueTask;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class QuestsIntegration {
    public static TaskType KEY_VALUE_TASK;
    public static RewardType KEY_VALUE_REWARD;
    public static RewardType TIMER_REWARD;

    @SubscribeEvent
    public void registerTasks(RegistryEvent.Register<TaskType> event) {
        event.getRegistry().register(KEY_VALUE_TASK = new TaskType(KeyValueTask::new).setRegistryName("key_value").setIcon(Icon.getIcon("minecraft:items/paper")));
    }

    @SubscribeEvent
    public void registerRewards(RegistryEvent.Register<RewardType> event) {
        event.getRegistry().register(KEY_VALUE_REWARD = new RewardType(KeyValueReward::new).setRegistryName("key_value").setIcon(Icon.getIcon("minecraft:items/paper")));
        event.getRegistry().register(TIMER_REWARD = new RewardType(TimerReward::new).setRegistryName("timer").setIcon(Icon.getIcon("minecraft:items/clock_07")));
    }
}
