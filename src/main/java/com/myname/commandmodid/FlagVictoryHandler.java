package com.myname.commandmodid;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;

public class FlagVictoryHandler {

    public static void checkVictory() {
        // Получаем координаты точки флага
        int x = FlagPointCommand.flagPointX;
        int y = FlagPointCommand.flagPointY;
        int z = FlagPointCommand.flagPointZ;

        // Проверяем, стоит ли флаг на точке
        Block block = MinecraftServer.getServer().worldServers[0].getBlock(x, y, z);
        int meta = MinecraftServer.getServer().worldServers[0].getBlockMetadata(x, y, z);
        // кароче если стекло стоит а время кончилось = победа
        if (block == Blocks.stained_glass) {
            String colorName = FlagColors.getColorName(meta);
            String colorCode = FlagColors.getColorCode(meta);
            CommandMod.network.sendToAll(
                new PacketAnnouncement(
                    colorCode + StatCollector.translateToLocal("message.flag.victoryhandler.win") + colorName));
        }
    }

}
