package com.vecoo.extraquests;

import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extralib.util.PacketUtil;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.command.FeatureCommands;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.listener.ExtraQuestsListener;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.network.QuestClientEvents;
import com.vecoo.extraquests.network.QuestClientState;
import com.vecoo.extraquests.network.QuestStatePayload;
import com.vecoo.extraquests.runtime.QuestFeatures;
import com.vecoo.extraquests.util.PermissionNodes;
import lombok.Getter;
import lombok.val;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
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
    @NotNull
    private ServerConfig serverConfig;

    @Getter
    @NotNull
    private LocaleConfig localeConfig;

    public ExtraQuests(IEventBus modEventBus) {
        instance = this;

        loadConfig();

        QuestFeatures.registerBase();
        QuestFeatures.register(PacketUtil::sendToPlayer);

        NeoForge.EVENT_BUS.register(new ExtraQuestsListener());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(new QuestClientEvents());
        }

        modEventBus.addListener(this::onRegisterPayloads);

        NeoForge.EVENT_BUS.register(this);
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

    private void onRegisterPayloads(@NotNull RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(QuestStatePayload.TYPE, QuestStatePayload.CODEC, (payload, context) -> QuestClientState.update(payload.data()));
    }

    @NotNull
    public static Logger getLogger() {
        return LOGGER;
    }
}
