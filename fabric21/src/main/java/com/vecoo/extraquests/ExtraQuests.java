package com.vecoo.extraquests;

import com.mojang.logging.LogUtils;
import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.IOException;

public class ExtraQuests implements ModInitializer {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Getter
    private static ExtraQuests instance;

    private ServerConfig serverConfig;
    private LocaleConfig localeConfig;

    private MinecraftServer server;

    @Override
    public void onInitialize() {
        instance = this;

        loadConfig();
        registerQuests();

        CommandRegistrationCallback.EVENT.register(ExtraQuestsCommand::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);
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
        KeyValueTask.TYPE = TaskTypes.register(ResourceLocation.fromNamespaceAndPath(ExtraQuests.MOD_ID, "key_value"),
                KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(ResourceLocation.fromNamespaceAndPath(ExtraQuests.MOD_ID, "key_value"),
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