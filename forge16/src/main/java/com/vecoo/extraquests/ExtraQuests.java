package com.vecoo.extraquests;

import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.reward.TimerReward;
import com.vecoo.extraquests.service.PlayerService;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogManager.getLogger();

    @Getter
    private static ExtraQuests instance;

    private ServerConfig serverConfig;
    private LocaleConfig localeConfig;

    private PlayerService playerService;

    private MinecraftServer server;

    public ExtraQuests() {
        instance = this;

        loadConfig();
        registerQuests();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onFMLServerStarting(FMLServerStartingEvent event) {
        this.server = event.getServer();

        PermissionAPI.registerNode("minecraft.command.extraquests", DefaultPermissionLevel.OP, "");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ExtraQuestsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onFMLServerStarted(FMLServerStartedEvent event) {
        loadStorage();
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        this.playerService.save(true);
    }

    public void loadConfig() {
        try {
            this.serverConfig = YamlLoader.load(ServerConfig.class, "config/extraquests/config.yml", false);
            this.localeConfig = YamlLoader.load(LocaleConfig.class, "config/extraquests/locale.yml", false);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void loadStorage() {
        this.playerService = new PlayerService("%directory%/storage/extraquests/", this.server);

        try {
            this.playerService.init();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void registerQuests() {
        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
        TimerReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "timer"), TimerReward::new, () -> Icon.getIcon("minecraft:item/clock_07"));
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

    public PlayerService getPlayerService() {
        return instance.playerService;
    }

    public MinecraftServer getServer() {
        return instance.server;
    }
}