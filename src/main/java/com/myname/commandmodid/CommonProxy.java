package com.myname.commandmodid;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        CommandMod.LOG.info(Config.greeting);
        CommandMod.LOG.info("I am CommandMod at version " + Tags.VERSION);
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new MFlagPointCommand());
        event.registerServerCommand(new TpFlagCommand());
    }

    public void registerNetworkHandlers() {
        CommandMod.network.registerMessage(PacketTimerText.Handler.class, PacketTimerText.class, 0, Side.CLIENT);
        CommandMod.network.registerMessage(PacketAnnouncement.Handler.class, PacketAnnouncement.class, 1, Side.CLIENT);
    }
}
