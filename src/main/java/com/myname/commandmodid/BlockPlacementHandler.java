package com.myname.commandmodid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

public class BlockPlacementHandler {

    public static boolean flagplaced = false;
    private static final Set<Block> ALLOWED_BLOCKS = new HashSet<>(Arrays.asList(Blocks.stained_glass, Blocks.wool));

    private static final String[] COLOR_NAMES = { "белая", "оранжевая", "пурпурная", "голубая", "желтая", "лаймовая",
        "розовая", "серая", "светло-серая", "бирюзовая", "фиолетовая", "синяя", "коричневая", "зеленая", "красная",
        "черная" };
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

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        int x = event.x, y = event.y, z = event.z;
        Block block = event.block;

        // Проверяем только координаты и тип блока
        if (isFlagPoint(x, y, z) && ALLOWED_BLOCKS.contains(block)) {
            flagplaced = false;
            PhaseActionBarTimer.stop();
            if (FMLCommonHandler.instance()
                .getEffectiveSide() == Side.SERVER) {
                CommandMod.network.sendToAll(new PacketTimerText(""));
            }
            if (event.getPlayer() != null) {
                CommandMod.network.sendToAll(new PacketAnnouncement("Флаг был сброшен!"));

            }

        }
    }

    @SubscribeEvent
    public void onBlockPlace(PlayerInteractEvent event) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return;
        if (FMLCommonHandler.instance()
            .getEffectiveSide() != Side.SERVER) return;

        if (event.action != Action.RIGHT_CLICK_BLOCK) return;
        EntityPlayer player = event.entityPlayer;
        int x = event.x, y = event.y, z = event.z;
        int placeX = x, placeY = y, placeZ = z;
        switch (event.face) {
            case 0:
                placeY--;
                break; // низ
            case 1:
                placeY++;
                break; // верх
            case 2:
                placeZ--;
                break; // север
            case 3:
                placeZ++;
                break; // юг
            case 4:
                placeX--;
                break; // запад
            case 5:
                placeX++;
                break; // восток
        }

        // Проверяем, что ставят блок ИМЕННО на координаты флага
        if (!isFlagPoint(placeX, placeY, placeZ)) {
            return;
        }

        if (FlagPointCommand.preparationPhase) {
            CommandMod.network.sendTo(
                new PacketPersonalMessage("Установка блоков запрещена во время фазы подготовки!"),
                (EntityPlayerMP) player);
            event.setCanceled(true);
            return;
        }

        ItemStack itemStack = player.getCurrentEquippedItem();
        if (itemStack == null) {
            event.setCanceled(true);
            return;
        }

        Block block = Block.getBlockFromItem(itemStack.getItem());
        if (ALLOWED_BLOCKS.contains(block)) {
            int metadata = itemStack.getItemDamage();
            CommandMod.network.sendToAll(
                new PacketAnnouncement(
                    getColorCode(metadata) + "Команда " + getColorName(metadata) + " поставила флаг!"));
            flagplaced = true;
            PhaseActionBarTimer.start();

        } else {
            CommandMod.network
                .sendTo(new PacketPersonalMessage("Можно ставить только шерсть или стекло!"), (EntityPlayerMP) player);
            event.setCanceled(true);
        }
    }

    private boolean isFlagPoint(int x, int y, int z) {
        return x == FlagPointCommand.flagPointX && y == FlagPointCommand.flagPointY && z == FlagPointCommand.flagPointZ;
    }

    private String getColorName(int metadata) {
        return (metadata >= 0 && metadata < COLOR_NAMES.length) ? COLOR_NAMES[metadata] : "неизвестная";
    }

    private static String getColorCode(int meta) {
        return (meta >= 0 && meta < COLOR_CODES.length) ? COLOR_CODES[meta] : "§f";
    }
}
