package com.vecoo.extraquests;

import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.timer.ListingProvider;
import com.vecoo.extraquests.util.TimerProvider;
import com.vecoo.extraquests.util.Utils;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";

    private static ExtraQuests instance;

    private ListingProvider listingsProvider;

    private TimerProvider timer = new TimerProvider();

    public ExtraQuests() {
        instance = this;

        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueTask::new,
                () -> Icon.getIcon("minecraft:item/paper"));

        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueReward::new,
                () -> Icon.getIcon("minecraft:item/paper"));

        TimerReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "timer"), TimerReward::new,
                () -> Icon.getIcon("minecraft:item/clock_07"));

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void loadConfig(ServerStartedEvent event) {
        try {
            this.listingsProvider = new ListingProvider();
            this.listingsProvider.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onServerStop(ServerStoppingEvent event) {
        Utils.removeAllTimers();
    }

    public static ExtraQuests getInstance() {
        return instance;
    }

    public ListingProvider getListingsProvider() {
        return this.listingsProvider;
    }

    public TimerProvider getTimerProvider() {
        return this.timer;
    }
}