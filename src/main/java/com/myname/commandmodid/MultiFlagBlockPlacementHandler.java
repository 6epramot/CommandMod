package com.myname.commandmodid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class MultiFlagBlockPlacementHandler {

    // Обработчик установки блока
    @SubscribeEvent
    public void onBlockPlace(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (event.entityPlayer.worldObj.isRemote) return;

        ItemStack itemStack = event.entityPlayer.getCurrentEquippedItem();
        if (itemStack == null) return;

        Block block = Block.getBlockFromItem(itemStack.getItem());
        int x = event.x, y = event.y, z = event.z;
        int colorMeta = itemStack.getItemDamage();

        // Корректируем координаты в зависимости от стороны установки
        switch (event.face) {
            case 0:
                y--;
                break;
            case 1:
                y++;
                break;
            case 2:
                z--;
                break;
            case 3:
                z++;
                break;
            case 4:
                x--;
                break;
            case 5:
                x++;
                break;
        }

        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            // Проверяем, совпадают ли координаты с флагом
            if (flag.x == x && flag.y == y && flag.z == z) {
                // Проверяем, что блок — это стекло
                if (block != Blocks.stained_glass) {
                    if (event.entityPlayer instanceof EntityPlayerMP) {
                        CommandMod.network.sendTo(
                            new PacketPersonalMessage("На месте флага можно ставить только цветное стекло!"),
                            (EntityPlayerMP) event.entityPlayer);
                    }
                    event.setCanceled(true);
                    return;
                }

                // Проверяем корректность цвета
                if (colorMeta < 0 || colorMeta > 15) {
                    System.err.println("Ошибка: некорректный цвет стекла: " + colorMeta);
                    event.setCanceled(true);
                    return;
                }

                // Обновляем цвет флага и статус flagplaced
                System.out.println("Установка флага: " + flag.name + " на координаты: " + x + ", " + y + ", " + z);
                MFlagPointCommand.setFlagColor(flag.name, colorMeta);
                flag.flagplaced = true;

                // Синхронизируем флаги с клиентами
                syncFlagsToAll();

                // Отправляем сообщение игрокам в зоне
                List<EntityPlayerMP> playersInZone = getPlayersInHemisphere(event.world, flag);
                for (EntityPlayerMP player : playersInZone) {
                    CommandMod.network
                        .sendTo(new PacketPersonalMessage("Флаг " + flag.name + " был установлен!"), player);
                }

                // Запускаем таймер захвата
                MultiFlagTimerManager
                    .startFlagTimer(flag.name, MFlagPointCommand.flagCaptureTime, playersInZone, colorMeta);
                return;
            }
        }
    }

    // Обработчик разрушения блока
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = event.world;
        if (world.isRemote) return;

        int x = event.x, y = event.y, z = event.z;

        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            // Проверяем, совпадают ли координаты с флагом
            if (flag.x == x && flag.y == y && flag.z == z) {
                // Сбрасываем цвет флага и статус flagplaced
                System.out.println("Флаг " + flag.name + " был разрушен на координатах: " + x + ", " + y + ", " + z);
                MFlagPointCommand.setFlagColor(flag.name, -1);
                flag.flagplaced = false;

                // Синхронизируем флаги с клиентами
                syncFlagsToAll();

                // Собираем игроков в сферической зоне
                List<EntityPlayerMP> playersInZone = getPlayersInHemisphere(world, flag);

                // Удаляем таймер и отправляем сообщение игрокам в зоне
                MultiFlagTimerManager.stopFlagTimer(flag.name, playersInZone);
                for (EntityPlayerMP player : playersInZone) {
                    CommandMod.network
                        .sendTo(new PacketPersonalMessage("Захват флага " + flag.name + " был прерван!"), player);
                }
                return;
            }
        }
    }

    // Метод для проверки, стоит ли флаг на месте (вызов из onServerTick)
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Проходим по всем флагам
        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            if (flag.flagplaced) { // Проверяем только флаги, которые установлены
                World world = DimensionManager.getWorld(0); // Получаем мир Overworld
                if (world == null) {
                    System.err.println("Ошибка: мир Overworld равен null.");
                    continue;
                }

                // Проверяем, есть ли стекло на месте флага
                Block block = world.getBlock(flag.x, flag.y, flag.z);
                if (block != Blocks.stained_glass) {
                    // Если стекло отсутствует, сбрасываем флаг
                    System.out.println(
                        "Флаг " + flag.name
                            + " больше не существует на координатах: "
                            + flag.x
                            + ", "
                            + flag.y
                            + ", "
                            + flag.z);

                    // Сбрасываем цвет флага и статус flagplaced
                    MFlagPointCommand.setFlagColor(flag.name, -1);
                    flag.flagplaced = false;

                    // Синхронизируем флаги с клиентами
                    syncFlagsToAll();

                    // Собираем игроков в зоне полусферы
                    List<EntityPlayerMP> playersInZone = getPlayersInHemisphere(world, flag);

                    // Удаляем таймер и отправляем сообщение игрокам в зоне
                    MultiFlagTimerManager.stopFlagTimer(flag.name, playersInZone);
                    for (EntityPlayerMP player : playersInZone) {
                        CommandMod.network
                            .sendTo(new PacketPersonalMessage("Захват флага " + flag.name + " был прерван!"), player);
                    }
                }
            }
        }
    }

    // Метод для получения игроков в зоне полусферы
    private List<EntityPlayerMP> getPlayersInHemisphere(World world, MFlagPointCommand.FlagData flag) {
        List<EntityPlayerMP> playersInZone = new ArrayList<>();
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
                if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= MFlagPointCommand.flagHemisphereRadius) {
                    playersInZone.add(player);
                }
            }
        }
        return playersInZone;
    }

    // Метод для синхронизации флагов с клиентами
    private void syncFlagsToAll() {
        CommandMod.network.sendToAll(new PacketAllFlags(MFlagPointCommand.getAllFlags()));
    }
}
