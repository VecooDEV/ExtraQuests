package com.vecoo.extraquests;

import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.util.PermissionNodes;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import lombok.Getter;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = ExtraQuestsLogger.getLogger();

    @Getter
    @NotNull
    private static ExtraQuests instance;

    @Getter
    @NotNull
    private ServerConfig serverConfig;

    @Getter
    @NotNull
    private LocaleConfig localeConfig;

    public ExtraQuests() {
        instance = this;

        loadConfig();
        registerQuests();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPermissionGather(@NotNull PermissionGatherEvent.Nodes event) {
        PermissionNodes.registerPermission(event);
    }

    @SubscribeEvent
    public void onRegisterCommands(@NotNull RegisterCommandsEvent event) {
        ExtraQuestsCommand.register(event.getDispatcher());
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

    private void registerQuests() {
        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"),
                KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"),
                KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
    }

    @NotNull
    public static Logger getLogger() {
        return LOGGER;
    }
}
