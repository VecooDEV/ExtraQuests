package com.vecoo.extraquests;

import com.vecoo.extraquests.integration.ExtraIntegration;
import com.vecoo.extraquests.timer.ListingProvider;
import com.vecoo.extraquests.timer.TimerProvider;
import com.vecoo.extraquests.util.Utils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ExtraQuests.MOD_ID, acceptableRemoteVersions = "*", useMetadata = true)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogManager.getLogger("ExtraQuests");

    private static ExtraQuests instance;

    private ListingProvider listingsProvider;

    private TimerProvider timer = new TimerProvider();

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        instance = this;
    }

    @Mod.EventHandler
    public void onInitialization(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ExtraIntegration());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        this.loadConfig();
    }

    public void loadConfig() {
        try {
            this.listingsProvider = new ListingProvider();
            this.listingsProvider.init();
        } catch (Exception e) {
            LOGGER.error("Error load config.");
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        Utils.removeAllTimers();
    }

    public static ExtraQuests getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public ListingProvider getListingsProvider() {
        return instance.listingsProvider;
    }

    public TimerProvider getTimerProvider() {
        return instance.timer;
    }
}