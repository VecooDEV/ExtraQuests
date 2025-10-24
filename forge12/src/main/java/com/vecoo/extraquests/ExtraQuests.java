package com.vecoo.extraquests;

import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.integration.QuestsIntegration;
import com.vecoo.extraquests.storage.TimerProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ExtraQuests.MOD_ID, acceptableRemoteVersions = "*", useMetadata = true)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static Logger LOGGER;

    private static ExtraQuests instance;

    private ServerConfig config;
    private LocaleConfig locale;

    private TimerProvider timerProvider;

    private MinecraftServer server;

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        instance = this;
        LOGGER = event.getModLog();

        MinecraftForge.EVENT_BUS.register(new QuestsIntegration());
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        this.server = event.getServer();
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        loadStorage();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        this.timerProvider.write();
    }

    public void loadConfig() {
        try {
            this.config = new ServerConfig();
            this.config.init();
            this.locale = new LocaleConfig();
            this.locale.init();
        } catch (Exception e) {
            LOGGER.error("Error load config.", e);
        }
    }

    public void loadStorage() {
        try {
            if (this.timerProvider == null) {
                this.timerProvider = new TimerProvider("/%directory%/storage/ExtraQuests/", this.server);
            }

            this.timerProvider.init();
        } catch (Exception e) {
            LOGGER.error("Error load storage.", e);
        }
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