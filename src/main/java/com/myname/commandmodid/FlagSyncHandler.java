package com.myname.commandmodid;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class FlagSyncHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (FlagPointCommand.flagPointSet && event.player instanceof EntityPlayerMP) {
            // Отправляем актуальные координаты и цвет флага только что зашедшему игроку
            CommandMod.network.sendTo(
                new PacketFlagBeam(
                    FlagPointCommand.flagPointX,
                    FlagPointCommand.flagPointY,
                    FlagPointCommand.flagPointZ,
                    BlockPlacementHandler.getFlagColorIndex(),
                    true),
                (EntityPlayerMP) event.player);
        }
        if (event.player instanceof EntityPlayerMP) {
            CommandMod.network
                .sendTo(new PacketAllFlags(MFlagPointCommand.getAllFlags()), (EntityPlayerMP) event.player);
        }
    }

}
