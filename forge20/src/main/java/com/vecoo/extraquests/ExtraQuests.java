package com.vecoo.extraquests;

import com.mojang.logging.LogUtils;
import com.vecoo.extralib.config.YamlConfigFactory;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.service.QuestTimerService;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.util.PermissionNodes;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import org.slf4j.Logger;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Getter
    private static ExtraQuests instance;

    private ServerConfig serverConfig;
    private LocaleConfig localeConfig;

    private QuestTimerService questTimerService;

    private MinecraftServer server;

    public ExtraQuests() {
        instance = this;

        loadConfig();
        registerQuests();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPermissionGather(PermissionGatherEvent.Nodes event) {
        PermissionNodes.registerPermission(event);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ExtraQuestsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        loadStorage();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        this.questTimerService.save();
    }

    public void loadConfig() {
        this.serverConfig = YamlConfigFactory.load(ServerConfig.class, "config/ExtraQuests/config.yml");
        this.localeConfig = YamlConfigFactory.load(LocaleConfig.class, "config/ExtraQuests/locale.yml");
    }

    private void loadStorage() {
        try {
            this.questTimerService = new QuestTimerService("/%directory%/storage/ExtraQuests/", this.server);
            this.questTimerService.init();
        } catch (Exception e) {
            LOGGER.error("Error load storage.", e);
        }
    }

    private void registerQuests() {
        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"),
                KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"),
                KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
        TimerReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "timer"),
                TimerReward::new, () -> Icon.getIcon("minecraft:item/clock_07"));
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public ServerConfig getServerConfig() {
        return instance.serverConfig;
    }

    public LocaleConfig getLocaleConfig() {
        return instance.localeConfig;
    }

    public QuestTimerService getQuestTimerService() {
        return instance.questTimerService;
    }

    public MinecraftServer getServer() {
        return instance.server;
    }
}