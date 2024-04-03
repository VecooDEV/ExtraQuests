package com.vecoo.extraquests;

import com.vecoo.extraquests.listener.RegisterFTB;
import com.vecoo.extraquests.timer.ListingProvider;
import com.vecoo.extraquests.timer.TimerProvider;
import com.vecoo.extraquests.util.Task;
import com.vecoo.extraquests.util.Utils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

@Mod(
        modid = "extraquests",
        name = "ExtraQuests",
        version = "1.1.1",
        acceptableRemoteVersions = "*"
)
public class ExtraQuests {
    private static MinecraftServer server;

    private static ListingProvider listingsProvider;

    private static TimerProvider timer = new TimerProvider();

    @Mod.EventHandler
    public void onServerStarting(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RegisterFTB());
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        server = event.getServer();
    }

    @Mod.EventHandler
    public void loadConfig(FMLServerStartedEvent event) {
        Task.builder()
                .execute(() -> {
                    try {
                        listingsProvider = new ListingProvider();
                        listingsProvider.init();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .delay(20L * 5)
                .interval(20L * 5)
                .build();
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) {
        Utils.removeAllTimers();
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static ListingProvider getListingsProvider() {
        return listingsProvider;
    }

    public static TimerProvider getTimerProvider() {
        return timer;
    }
}