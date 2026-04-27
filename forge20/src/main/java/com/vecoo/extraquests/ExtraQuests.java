package com.vecoo.extraquests;

import com.mojang.logging.LogUtils;
import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.reward.KeyValueReward;
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
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Getter
    private static ExtraQuests instance;

    private ServerConfig serverConfig;
    private LocaleConfig localeConfig;

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
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ExtraQuestsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
    }

    public void loadConfig() {
        try {
            this.serverConfig = YamlLoader.load(ServerConfig.class, "config/extraquests/config.yml", false);
            this.localeConfig = YamlLoader.load(LocaleConfig.class, "config/extraquests/locale.yml", false);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void registerQuests() {
        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"),
                KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"),
                KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
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

    public MinecraftServer getServer() {
        return instance.server;
    }
}