package com.myname.commandmodid;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        CommandMod.LOG.info(Config.greeting);
        CommandMod.LOG.info("I am CommandMod at version " + Tags.VERSION);
    }

    // Do your mod setup. Build whatever data structures you care about. Register
    // recipes.
    public void init(FMLInitializationEvent event) {}

    // Handle interaction with other mods, complete your setup based on this.
    public void postInit(FMLPostInitializationEvent event) {}

    // Register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {}

    public void registerNetworkHandlers() {
        // Register packet handlers here
        CommandMod.network.registerMessage(PacketTimerText.Handler.class, PacketTimerText.class, 0, Side.CLIENT);
        CommandMod.network.registerMessage(PacketAnnouncement.Handler.class, PacketAnnouncement.class, 1, Side.CLIENT);
    }
}
