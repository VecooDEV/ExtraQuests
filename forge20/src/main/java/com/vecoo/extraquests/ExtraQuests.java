package com.vecoo.extraquests;

import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.command.FeatureCommands;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.listener.ExtraQuestsListener;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.network.ExtraQuestsClientListener;
import com.vecoo.extraquests.network.QuestStatePayload;
import com.vecoo.extraquests.runtime.QuestFeatures;
import com.vecoo.extraquests.util.PermissionNodes;
import lombok.Getter;
import lombok.val;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = ExtraQuestsConstants.MOD_ID;
    private static final Logger LOGGER = ExtraQuestsLogger.getLogger();

    @Getter
    private static ExtraQuests instance;

    @Getter
    private ServerConfig serverConfig;
    @Getter
    private LocaleConfig localeConfig;

    public ExtraQuests() {
        instance = this;

        loadConfig();

        QuestFeatures.registerBase();
        QuestFeatures.register((player, payload) -> payload.sendTo(player));
        QuestStatePayload.register();

        MinecraftForge.EVENT_BUS.register(new ExtraQuestsListener());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ExtraQuestsClientListener());
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPermissionGather(@NotNull PermissionGatherEvent.Nodes event) {
        PermissionNodes.registerPermission(event);
    }

    @SubscribeEvent
    public void onRegisterCommands(@NotNull RegisterCommandsEvent event) {
        ExtraQuestsCommand.register(event.getDispatcher());
        FeatureCommands.register(event.getDispatcher());
    }


    @SubscribeEvent
    public void onStopped(@NotNull ServerStoppedEvent event) {
        QuestFeatures.stopped();
    }

    public void loadConfig() {
        try {
            val serverConfig = YamlLoader.load(ServerConfig.class, "config/extraquests/config.yml", false);
            val localeConfig = YamlLoader.load(LocaleConfig.class, "config/extraquests/locale.yml", false);

            if (serverConfig == null) {
                throw new IOException("Failed to load config/extraquests/config.yml: loader returned null.");
            }

            if (localeConfig == null) {
                throw new IOException("Failed to load config/extraquests/locale.yml: loader returned null.");
            }

            this.serverConfig = serverConfig;
            this.localeConfig = localeConfig;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @NotNull
    public static Logger getLogger() {
        return LOGGER;
    }
}
