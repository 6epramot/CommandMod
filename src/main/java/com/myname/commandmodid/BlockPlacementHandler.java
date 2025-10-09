package com.myname.commandmodid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class BlockPlacementHandler {

    public static boolean flagplaced = false;
    private static final Set<Block> ALLOWED_BLOCKS = new HashSet<>(Arrays.asList(Blocks.stained_glass));
    // дефолтный цвет белый
    private static int flagColorIndex = 0;

    // Проверяет, совпадают ли координаты с точкой флага
    public static void setFlagColorIndex(int idx) {
        flagColorIndex = (idx >= 0 && idx < FlagColors.COLORS.length) ? idx : 0;
    }

    public static int getFlagColorIndex() {
        return flagColorIndex;
    }

    public static float getFlagR() {
        return FlagColors.getColor(flagColorIndex)[0];
    }

    public static float getFlagG() {
        return FlagColors.getColor(flagColorIndex)[1];
    }

    public static float getFlagB() {
        return FlagColors.getColor(flagColorIndex)[2];
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        // Проверяем только если флаг был установлен
        if (flagplaced) {
            World world = DimensionManager.getWorld(0); // 0 — обычный мир (Overworld)
            if (world != null) {
                int x = FlagPointCommand.flagPointX;
                int y = FlagPointCommand.flagPointY;
                int z = FlagPointCommand.flagPointZ;
                Block block = world.getBlock(x, y, z);
                if (!ALLOWED_BLOCKS.contains(block)) {
                    flagplaced = false;
                    PhaseActionBarTimer.stop();
                    CommandMod.network.sendToAll(new PacketTimerText(""));
                    CommandMod.network.sendToAll(new PacketAnnouncement("Флаг был сброшен!"));
                    CommandMod.network.sendToAll(
                        new PacketFlagBeam(
                            FlagPointCommand.flagPointX,
                            FlagPointCommand.flagPointY,
                            FlagPointCommand.flagPointZ,
                            0,
                            true));
                }
            }
        }
        if (FlagPointCommand.flagPointSet) {
            World world = DimensionManager.getWorld(0); // 0 — Overworld
            if (world != null) {
                int x = FlagPointCommand.flagPointX;
                int y = FlagPointCommand.flagPointY;
                int z = FlagPointCommand.flagPointZ;
                for (int dy = 1; dy <= 20; dy++) {
                    int checkY = y + dy;
                    Block block = world.getBlock(x, checkY, z);
                    if (block != Blocks.air) {
                        world.setBlock(x, checkY, z, Blocks.air, 0, 2); // 2 — обновить клиентам
                    }
                }
            }
        }
    }

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
                CommandMod.network.sendToAll(
                    new PacketFlagBeam(
                        FlagPointCommand.flagPointX,
                        FlagPointCommand.flagPointY,
                        FlagPointCommand.flagPointZ,
                        0,
                        true));
            }

        }
    }

    @SubscribeEvent
    public void onBlockPlace(PlayerInteractEvent event) {
        if (FlagPointCommand.flagPointSet && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            // Только на сервере!
            if (event.entityPlayer.worldObj.isRemote) return;

            int flagX = FlagPointCommand.flagPointX;
            int flagY = FlagPointCommand.flagPointY;
            int flagZ = FlagPointCommand.flagPointZ;

            int placeX = event.x;
            int placeY = event.y;
            int placeZ = event.z;

            switch (event.face) {
                case 0:
                    placeY--;
                    break;
                case 1:
                    placeY++;
                    break;
                case 2:
                    placeZ--;
                    break;
                case 3:
                    placeZ++;
                    break;
                case 4:
                    placeX--;
                    break;
                case 5:
                    placeX++;
                    break;
            }

            if (placeX == flagX && placeZ == flagZ && placeY > flagY && placeY <= flagY + 20) {
                if (event.entityPlayer instanceof EntityPlayerMP) {
                    CommandMod.network.sendTo(
                        new PacketPersonalMessage("Над флагом нельзя строить"),
                        (EntityPlayerMP) event.entityPlayer);
                }
                event.setCanceled(true);
            }
        }

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
                break;
            case 1:
                placeY++;
                break;
            case 2:
                placeZ--;
                break;
            case 3:
                placeZ++;
                break;
            case 4:
                placeX--;
                break;
            case 5:
                placeX++;
                break;
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
                    FlagColors.getColorCode(metadata) + "Команда "
                        + FlagColors.getColorName(metadata)
                        + " поставила флаг!"));
            CommandMod.network.sendToAll(
                new PacketFlagBeam(
                    FlagPointCommand.flagPointX,
                    FlagPointCommand.flagPointY,
                    FlagPointCommand.flagPointZ,
                    metadata,
                    true));
            flagplaced = true;
            setFlagColorIndex(metadata);
            PhaseActionBarTimer.start();

        } else {
            CommandMod.network
                .sendTo(new PacketPersonalMessage("Можно ставить только стекло!"), (EntityPlayerMP) player);
            event.setCanceled(true);
        }
    }

    private boolean isFlagPoint(int x, int y, int z) {
        return x == FlagPointCommand.flagPointX && y == FlagPointCommand.flagPointY && z == FlagPointCommand.flagPointZ;
    }
}
