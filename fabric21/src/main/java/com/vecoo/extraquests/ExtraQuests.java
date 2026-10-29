package com.vecoo.extraquests;

import com.vecoo.extralib.loader.YamlLoader;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.runtime.QuestFeatures;
import lombok.Getter;
import lombok.val;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class ExtraQuests implements ModInitializer {
    public static final String MOD_ID = ExtraQuestsConstants.MOD_ID;
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

    @Override
    public void onInitialize() {
        instance = this;

        loadConfig();
        QuestFeatures.registerBase();

        CommandRegistrationCallback.EVENT.register(ExtraQuestsCommand::register);
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
