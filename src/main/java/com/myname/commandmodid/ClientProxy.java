package com.myname.commandmodid;

import net.minecraftforge.common.MinecraftForge;

import com.myname.commandmodid.timers.TimerOverlay;
import com.myname.commandmodid.utils.BeaconBeamHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    // регистрируем обработчики эвентов для клиента
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new TimerOverlay());
        MinecraftForge.EVENT_BUS.register(new BeaconBeamHandler());
    }
}
