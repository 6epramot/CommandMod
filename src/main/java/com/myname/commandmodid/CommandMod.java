package com.myname.commandmodid;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.myname.commandmodid.multiFlags.FlagListCommand;
import com.myname.commandmodid.multiFlags.MFlagPointCommand;
import com.myname.commandmodid.multiFlags.MultiFlagBlockPlacementHandler;
import com.myname.commandmodid.multiFlags.TpFlagCommand;
import com.myname.commandmodid.packets.PacketAllFlags;
import com.myname.commandmodid.packets.PacketAnnouncement;
import com.myname.commandmodid.packets.PacketFlagBeam;
import com.myname.commandmodid.packets.PacketMultiFlagTimer;
import com.myname.commandmodid.packets.PacketPersonalMessage;
import com.myname.commandmodid.packets.PacketTimerText;
import com.myname.commandmodid.soloFlag.BlockPlacementHandler;
import com.myname.commandmodid.soloFlag.FlagPointCommand;
import com.myname.commandmodid.timers.MultiFlagTimerManager;
import com.myname.commandmodid.utils.FlagSyncHandler;

import cpw.mods.fml.common.FMLCommonHandler;
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

// Основной класс мода где регистрируем всё в сети и для правльной работы и обработки форджа
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
        // по порядочку главное расписывать пакетики
        proxy.init(event);
        network.registerMessage(PacketTimerText.Handler.class, PacketTimerText.class, 0, Side.CLIENT);
        network.registerMessage(PacketAnnouncement.Handler.class, PacketAnnouncement.class, 1, Side.CLIENT);
        network.registerMessage(PacketPersonalMessage.Handler.class, PacketPersonalMessage.class, 2, Side.CLIENT);
        network.registerMessage(PacketFlagBeam.Handler.class, PacketFlagBeam.class, 3, Side.CLIENT);
        network.registerMessage(PacketAllFlags.Handler.class, PacketAllFlags.class, 4, Side.CLIENT);
        network.registerMessage(PacketMultiFlagTimer.Handler.class, PacketMultiFlagTimer.class, 5, Side.CLIENT);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    // если сервак есть то ивентики вот так вот надо
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new FlagPointCommand());
        event.registerServerCommand(new MFlagPointCommand());
        event.registerServerCommand(new TpFlagCommand());
        event.registerServerCommand(new FlagListCommand());
        MinecraftForge.EVENT_BUS.register(new BlockPlacementHandler());
        MinecraftForge.EVENT_BUS.register(new MultiFlagBlockPlacementHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new MultiFlagTimerManager());
        FMLCommonHandler.instance()
            .bus()
            .register(new BlockPlacementHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new FlagSyncHandler());

    }
}
