package com.vecoo.extraquests;

import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.PermissionConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.timer.QuestTimerProvider;
import com.vecoo.extraquests.timer.TimerProvider;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogManager.getLogger("ExtraQuests");

    private static ExtraQuests instance;

    private LocaleConfig locale;
    private PermissionConfig permission;

    private QuestTimerProvider questTimerProvider;
    private TimerProvider timerProvider;

    private MinecraftServer server;

    public ExtraQuests() {
        instance = this;

        this.loadConfig();
        this.registerQuests();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ExtraQuestsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        this.loadStorage();
    }


    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        this.timerProvider.removeAllTimers();
    }

    public void loadConfig() {
        try {
            this.locale = new LocaleConfig();
            this.locale.init();
            this.permission = new PermissionConfig();
            this.permission.init();
        } catch (Exception e) {
            LOGGER.error("[ExtraQuests] Error load config.");
        }
    }

    public void loadStorage() {
        try {
            this.questTimerProvider = new QuestTimerProvider("/%directory%/storage/ExtraQuests/", this.server);
            this.questTimerProvider.init();
            this.timerProvider = new TimerProvider();
        } catch (Exception e) {
            LOGGER.error("[ExtraQuests] Error load storage.");
        }
    }

    public void registerQuests() {
        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
        TimerReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "timer"), TimerReward::new, () -> Icon.getIcon("minecraft:item/clock_07"));
    }

    public static ExtraQuests getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public LocaleConfig getLocale() {
        return instance.locale;
    }

    public PermissionConfig getPermission() {
        return instance.permission;
    }

    public QuestTimerProvider getQuestTimerProvider() {
        return instance.questTimerProvider;
    }

    public TimerProvider getTimerProvider() {
        return instance.timerProvider;
    }

    public MinecraftServer getServer() {
        return instance.server;
    }
}