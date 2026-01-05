package com.vecoo.extraquests;

import com.vecoo.extraquests.integration.QuestsIntegration;
import com.vecoo.extraquests.service.QuestTimerService;
import lombok.Getter;
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

    @Getter
    private static ExtraQuests instance;

    private QuestTimerService questTimerService;

    private MinecraftServer server;

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        instance = this;
        LOGGER = event.getModLog();

        MinecraftForge.EVENT_BUS.register(new QuestsIntegration());
    }

    @Mod.EventHandler
    public void onFMLServerStarting(FMLServerStartingEvent event) {
        this.server = event.getServer();
    }

    @Mod.EventHandler
    public void onFMLServerStarted(FMLServerStartedEvent event) {
        loadStorage();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        this.questTimerService.save();
    }

    private void loadStorage() {
        try {
            this.questTimerService = new QuestTimerService("/%directory%/storage/ExtraQuests/", this.server);
            this.questTimerService.init();
        } catch (Exception e) {
            LOGGER.error("Error load storage.", e);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public QuestTimerService getQuestTimerService() {
        return instance.questTimerService;
    }

    public MinecraftServer getServer() {
        return instance.server;
    }
}