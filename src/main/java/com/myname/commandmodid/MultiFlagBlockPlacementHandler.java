package com.myname.commandmodid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class MultiFlagBlockPlacementHandler {

    // Обработчик установки блока
    @SubscribeEvent
    public void onBlockPlace(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.entityPlayer.worldObj.isRemote)
            return;

        ItemStack itemStack = event.entityPlayer.getCurrentEquippedItem();
        if (itemStack == null)
            return;

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
                                new PacketPersonalMessage(
                                        StatCollector.translateToLocal("message.flag.mfBPH.invalid_block_on_point")),
                                (EntityPlayerMP) event.entityPlayer);
                    }
                    event.setCanceled(true);
                    return;
                }

                // Проверяем корректность цвета
                if (colorMeta < 0 || colorMeta > 15) {
                    System.err.println(StatCollector.translateToLocal("message.flag.mfBPH.invalid_color")
                            .replace("{0}", Integer.toString(colorMeta)));
                    event.setCanceled(true);
                    return;
                }

                // Обновляем цвет флага и статус flagplaced
                System.out.println(StatCollector.translateToLocal("message.flag.admin_message_fplaced")
                        .replace("{0}", flag.name).replace("{1}", Integer.toString(x))
                        .replace("{2}", Integer.toString(y)).replace("{3}", Integer.toString(z)));
                MFlagPointCommand.setFlagColor(flag.name, colorMeta);
                flag.flagplaced = true;

                // Синхронизируем флаги с клиентами
                syncFlagsToAll();

                // Отправляем сообщение игрокам в зоне
                List<EntityPlayerMP> playersInZone = new ArrayList<>(
                        FlagUtilities.getPlayersInHemisphere(event.world, flag,
                                MFlagPointCommand.flagHemisphereRadius));
                for (EntityPlayerMP player : playersInZone) {
                    CommandMod.network
                            .sendTo(new PacketPersonalMessage(
                                    StatCollector.translateToLocal("message.flag.mflag.flag_has_been_placed")
                                            .replace("{0}", flag.name)),
                                    player);
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
        if (world.isRemote)
            return;

        int x = event.x, y = event.y, z = event.z;

        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            // Проверяем, совпадают ли координаты с флагом
            if (flag.x == x && flag.y == y && flag.z == z) {
                // Сбрасываем цвет флага и статус flagplaced
                System.out.println(StatCollector.translateToLocal("message.flag.admin_message_fdeleted")
                        .replace("{0}", flag.name).replace("{1}", Integer.toString(x))
                        .replace("{2}", Integer.toString(y)).replace("{3}", Integer.toString(z)));
                MFlagPointCommand.setFlagColor(flag.name, -1);
                flag.flagplaced = false;

                // Синхронизируем флаги с клиентами
                syncFlagsToAll();

                // Собираем игроков в зоне
                List<EntityPlayerMP> playersInZone = new ArrayList<>(
                        FlagUtilities.getPlayersInHemisphere(world, flag, MFlagPointCommand.flagHemisphereRadius));

                // Удаляем таймер и отправляем сообщение игрокам в зоне
                MultiFlagTimerManager.stopFlagTimer(flag.name, playersInZone);
                for (EntityPlayerMP player : playersInZone) {
                    CommandMod.network
                            .sendTo(new PacketPersonalMessage(
                                    StatCollector.translateToLocal("message.flag.mflag.flag_has_been_destroyed")
                                            .replace("{0}", flag.name)),
                                    player);
                }
                return;
            }
        }
    }

    // Метод для синхронизации флагов с клиентами
    private void syncFlagsToAll() {
        CommandMod.network.sendToAll(new PacketAllFlags(MFlagPointCommand.getAllFlags()));
    }
}
