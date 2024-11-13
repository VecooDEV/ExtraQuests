package com.vecoo.extraquests;

import com.vecoo.extralib.permission.UtilPermissions;
import com.vecoo.extraquests.command.ExtraQuestsCommand;
import com.vecoo.extraquests.config.LocaleConfig;
import com.vecoo.extraquests.config.PermissionConfig;
import com.vecoo.extraquests.config.ServerConfig;
import com.vecoo.extraquests.integration.ExtraIntegration;
import com.vecoo.extraquests.timer.QuestTimerProvider;
import com.vecoo.extraquests.timer.TimerProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ExtraQuests.MOD_ID, acceptableRemoteVersions = "*", useMetadata = true)
public class ExtraQuests {
    public static final String MOD_ID = "extraquests";
    private static final Logger LOGGER = LogManager.getLogger("ExtraQuests");

    private static ExtraQuests instance;

    private ServerConfig config;
    private LocaleConfig locale;
    private PermissionConfig permission;

    private QuestTimerProvider questTimerProvider;
    private TimerProvider timerProvider;

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        instance = this;

        this.loadConfig();
    }

    @Mod.EventHandler
    public void onInitialization(FMLInitializationEvent event) {
        UtilPermissions.registerPermission(permission.getPermissionCommand());

        MinecraftForge.EVENT_BUS.register(new ExtraIntegration());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ExtraQuestsCommand());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        this.loadStorage();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        this.timerProvider.removeAllTimers();
    }

    public void loadConfig() {
        try {
            this.config = new ServerConfig();
            this.config.init();
            this.locale = new LocaleConfig();
            this.locale.init();
            this.permission = new PermissionConfig();
            this.permission.init();
        } catch (Exception e) {
            LOGGER.error("[ExtraQuests] Error load config.");
        }
    }

    public void loadStorage() {
        try {
            this.questTimerProvider = new QuestTimerProvider();
            this.questTimerProvider.init();
            this.timerProvider = new TimerProvider();
        } catch (Exception e) {
            LOGGER.error("[ExtraQuests] Error load storage.");
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

    public PermissionConfig getPermission() {
        return instance.permission;
    }

    public QuestTimerProvider getQuestTimerProvider() {
        return instance.questTimerProvider;
    }

    public TimerProvider getTimerProvider() {
        return instance.timerProvider;
    }
}