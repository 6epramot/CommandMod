package com.myname.commandmodid.utils;

import net.minecraft.entity.player.EntityPlayerMP;

import com.myname.commandmodid.CommandMod;
import com.myname.commandmodid.multiFlags.MFlagPointCommand;
import com.myname.commandmodid.packets.PacketAllFlags;
import com.myname.commandmodid.packets.PacketFlagBeam;
import com.myname.commandmodid.soloFlag.BlockPlacementHandler;
import com.myname.commandmodid.soloFlag.FlagPointCommand;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class FlagSyncHandler {

    /**
     * Обработчик ивента захода игрока на сервер для синхронизации флага
     * синхронизация координат и цвета, вроде для того,
     * стоит не стоит стелко не нужно
     */

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
