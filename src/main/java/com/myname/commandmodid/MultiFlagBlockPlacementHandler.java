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

    MFlagPointCommand.FlagData data;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (data.flagplaced) {
            World world = DimensionManager.getWorld(0);
            if (world != null) {
                int x = data.x;
                int y = data.y;
                int z = data.z;
                Block block = world.getBlock(x, y, z);
                if (block != Blocks.stained_glass) {
                    data.flagplaced = false;
                    MFlagPointCommand.setFlagColor(data.name, -1);
                    CommandMod.network.sendToAll(new PacketAllFlags(MFlagPointCommand.getAllFlags()));
                    List<EntityPlayerMP> playersInZone = new ArrayList<EntityPlayerMP>();
                    for (Object obj : world.playerEntities) {
                        if (obj instanceof EntityPlayerMP) {
                            EntityPlayerMP player = (EntityPlayerMP) obj;
                            if (isPlayerInFlagHemisphere(player, data, MFlagPointCommand.flagHemisphereRadius)) {
                                playersInZone.add(player);
                                // CommandMod.network.sendTo(new PacketAnnouncement("Флаг был сброшен!"),
                                // player);
                                // CommandMod.network.sendTo(new PacketFlagBeam(data.x, data.y, data.z, 0,
                                // true), player);
                            }
                        }
                    }

                }
            }
        }

    }

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

            // Запрет на любые блоки кроме стекла на месте флага
            if (flag.x == x && flag.y == y && flag.z == z) {
                if (block != Blocks.stained_glass) {
                    if (event.entityPlayer instanceof EntityPlayerMP) {
                        CommandMod.network.sendTo(
                            new PacketPersonalMessage("На месте флага можно ставить только цветное стекло!"),
                            (EntityPlayerMP) event.entityPlayer);
                    }
                    event.setCanceled(true);
                    return;
                }
                // Меняем цвет флага на цвет установленного стекла
                MFlagPointCommand.setFlagColor(flag.name, colorMeta);
                CommandMod.network.sendToAll(new PacketAnnouncement("Стекло поставили " + flag.name));
                flag.flagplaced = true;
                // Сообщение и таймер только игрокам в зоне-полусфере
                List<EntityPlayerMP> playersInZone = new ArrayList<EntityPlayerMP>();
                String colorCode = FlagColors.getColorCode(colorMeta);
                String colorName = FlagColors.getColorName(colorMeta);
                for (Object obj : event.entityPlayer.worldObj.playerEntities) {

                    if (obj instanceof EntityPlayerMP) {
                        EntityPlayerMP player = (EntityPlayerMP) obj;
                        if (isPlayerInFlagHemisphere(player, flag, MFlagPointCommand.flagHemisphereRadius)) {
                            playersInZone.add(player);
                            CommandMod.network.sendTo(
                                new PacketPersonalMessage(
                                    colorCode + colorName + " команда установила флаг " + flag.name + "!"),
                                player);
                        }
                    }
                }
                MultiFlagTimerManager
                    .startFlagTimer(flag.name, MFlagPointCommand.flagCaptureTime, playersInZone, colorMeta);
                event.setCanceled(false); // Разрешаем установку стекла
                return;
            }
            // Запрет на любые блоки над флагом в диапазоне 10 блоков
            if (flag.x == x && flag.z == z && y > flag.y && y <= flag.y + 10) {
                if (event.entityPlayer instanceof EntityPlayerMP) {
                    CommandMod.network.sendTo(
                        new PacketPersonalMessage("Над флагом нельзя строить в радиусе 10 блоков!"),
                        (EntityPlayerMP) event.entityPlayer);
                }
                event.setCanceled(true);
                return;
            }
        }
    }

    private boolean isPlayerInFlagHemisphere(EntityPlayerMP player, MFlagPointCommand.FlagData flag, double radius) {
        double dx = player.posX - (flag.x + 0.5);
        double dy = player.posY - (flag.y + 0.5);
        double dz = player.posZ - (flag.z + 0.5);
        if (dy < 0) return false;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.world.isRemote) return;

        int x = event.x, y = event.y, z = event.z;
        Block block = event.block;

        if (block != Blocks.stained_glass) return;

        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            if (flag.x == x && flag.y == y && flag.z == z) {
                // Сброс цвета флага
                MFlagPointCommand.setFlagColor(flag.name, -1);
                // записываем в список игроков в зоне полусферы
                List<EntityPlayerMP> playersInZone = new ArrayList<EntityPlayerMP>();
                for (Object obj : event.world.playerEntities) {
                    if (obj instanceof EntityPlayerMP) {
                        EntityPlayerMP player = (EntityPlayerMP) obj;
                        if (isPlayerInFlagHemisphere(player, flag, MFlagPointCommand.flagHemisphereRadius)) {
                            playersInZone.add(player);

                        }
                    }
                }
                // удаление флага у игроков в зоне
                MultiFlagTimerManager.stopFlagTimer(flag.name, playersInZone);
                for (EntityPlayerMP player : playersInZone) {
                    CommandMod.network
                        .sendTo(new PacketPersonalMessage("Захват флага " + flag.name + " прерван!"), player);
                }
                break;
            }

        }

    }

}
