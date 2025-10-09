package com.myname.commandmodid;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;

public class FlagVictoryHandler {

    public static void checkVictory() {
        // Получаем координаты точки флага
        int x = FlagPointCommand.flagPointX;
        int y = FlagPointCommand.flagPointY;
        int z = FlagPointCommand.flagPointZ;

        // Проверяем, стоит ли флаг на точке (например, шерсть или стекло)
        Block block = MinecraftServer.getServer().worldServers[0].getBlock(x, y, z);
        int meta = MinecraftServer.getServer().worldServers[0].getBlockMetadata(x, y, z);

        if (block == Blocks.wool || block == Blocks.stained_glass) {
            String colorName = FlagColors.getColorName(meta);
            String colorCode = FlagColors.getColorCode(meta);
            CommandMod.network.sendToAll(new PacketAnnouncement(colorCode + "Победа команды " + colorName));
        }
    }

}
