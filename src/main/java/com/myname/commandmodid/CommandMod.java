package com.myname.commandmodid;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = CommandMod.MODID, version = Tags.VERSION, name = "CommandMod", acceptedMinecraftVersions = "[1.7.10]")
public class CommandMod {

    public static final String MODID = "commandmodid";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    @SidedProxy(clientSide = "com.myname.commandmodid.ClientProxy", serverSide = "com.myname.commandmodid.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        network.registerMessage(PacketTimerText.Handler.class, PacketTimerText.class, 0, Side.CLIENT);
        network.registerMessage(PacketAnnouncement.Handler.class, PacketAnnouncement.class, 1, Side.CLIENT);
        network.registerMessage(PacketPersonalMessage.Handler.class, PacketPersonalMessage.class, 2, Side.CLIENT);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new FlagPointCommand());
        MinecraftForge.EVENT_BUS.register(new BlockPlacementHandler());
    }
}
