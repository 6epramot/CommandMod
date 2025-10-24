package com.myname.commandmodid;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class FlagUtilities {

    /**
     * Получает список игроков в зоне полусферы вокруг флага.
     *
     * @param world  Мир, в котором находится флаг.
     * @param flag   Данные о флаге.
     * @param radius Радиус полусферы.
     * @return Список игроков в зоне полусферы.
     */
    // давно стоило в отдельный нах класс выделить
    // функция для получения игроков в сфере вокруг флага
    public static Set<EntityPlayerMP> getPlayersInHemisphere(World world, MFlagPointCommand.FlagData flag,
            double radius) {
        Set<EntityPlayerMP> playersInZone = new HashSet<>();
        if (world == null) {
            System.err.println("Ошибка: мир равен null.");
            return playersInZone;
        }
        for (Object obj : world.playerEntities) {
            if (obj instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) obj;
                double dx = player.posX - (flag.x + 0.5);
                double dy = player.posY - (flag.y + 0.5);
                double dz = player.posZ - (flag.z + 0.5);
                if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                    playersInZone.add(player);
                }
            }
        }
        return playersInZone;
    }
}
