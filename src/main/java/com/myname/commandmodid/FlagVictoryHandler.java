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
            String colorName = getColorName(meta);
            String colorCode = getColorCode(meta);
            CommandMod.network.sendToAll(
                    new PacketAnnouncement(colorCode + "Победа команды " + colorName));
        }
    }

    private static final String[] COLOR_CODES = { "§f", // белая
            "§6", // оранжевая
            "§d", // пурпурная
            "§b", // голубая
            "§e", // желтая
            "§a", // лаймовая
            "§d", // розовая (можно §d или §c)
            "§8", // серая
            "§7", // светло-серая
            "§3", // бирюзовая
            "§5", // фиолетовая
            "§9", // синяя
            "§6", // коричневая (ближайший — оранжевый)
            "§2", // зеленая
            "§c", // красная
            "§0" // черная
    };
    private static final String[] COLOR_NAMES = { "белая", "оранжевая", "пурпурная", "голубая", "желтая", "лаймовая",
            "розовая", "серая", "светло-серая", "бирюзовая", "фиолетовая", "синяя", "коричневая", "зеленая", "красная",
            "черная" };

    private static String getColorName(int meta) {
        return (meta >= 0 && meta < COLOR_NAMES.length) ? COLOR_NAMES[meta] : "неизвестная";
    }

    private static String getColorCode(int meta) {
        return (meta >= 0 && meta < COLOR_CODES.length) ? COLOR_CODES[meta] : "§f";
    }
}
