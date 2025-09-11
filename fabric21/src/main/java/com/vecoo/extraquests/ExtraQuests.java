package com.vecoo.extraquests;

import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.storage.TimerProvider;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExtraQuests implements ModInitializer {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogManager.getLogger("ExtraQuests");

    private static ExtraQuests instance;

    private ServerConfig config;
    private LocaleConfig locale;

    private TimerProvider timerProvider;

    private MinecraftServer server;

    @Override
    public void onInitialize() {
        instance = this;

        loadConfig();
        registerQuests();

        CommandRegistrationCallback.EVENT.register(ExtraQuestsCommand::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> loadStorage());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.timerProvider.write());
    }

    public void loadConfig() {
        try {
            this.config = new ServerConfig();
            this.config.init();
            this.locale = new LocaleConfig();
            this.locale.init();
        } catch (Exception e) {
            LOGGER.error("[ExtraQuests] Error load config.", e);
        }
    }

    public void loadStorage() {
        try {
            this.timerProvider = new TimerProvider("/%directory%/storage/ExtraQuests/", this.server);
            this.timerProvider.init();
        } catch (Exception e) {
            LOGGER.error("[ExtraQuests] Error load storage.", e);
        }
    }

    public void registerQuests() {
        KeyValueTask.TYPE = TaskTypes.register(ResourceLocation.fromNamespaceAndPath(ExtraQuests.MOD_ID, "key_value"), KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(ResourceLocation.fromNamespaceAndPath(ExtraQuests.MOD_ID, "key_value"), KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
        TimerReward.TYPE = RewardTypes.register(ResourceLocation.fromNamespaceAndPath(ExtraQuests.MOD_ID, "timer"), TimerReward::new, () -> Icon.getIcon("minecraft:item/clock_07"));
    }

    public static ExtraQuests getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public ServerConfig getConfig() {
        return instance.config;
    }

    public LocaleConfig getLocale() {
        return instance.locale;
    }

    public TimerProvider getTimerProvider() {
        return instance.timerProvider;
    }

    public MinecraftServer getServer() {
        return instance.server;
    }
}