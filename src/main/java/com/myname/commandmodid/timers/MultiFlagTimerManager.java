package com.myname.commandmodid.timers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.DimensionManager;

import com.myname.commandmodid.CommandMod;
import com.myname.commandmodid.multiFlags.MFlagPointCommand;
import com.myname.commandmodid.packets.PacketAnnouncement;
import com.myname.commandmodid.packets.PacketMultiFlagTimer;
import com.myname.commandmodid.utils.FlagColors;
import com.myname.commandmodid.utils.FlagUtilities;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class MultiFlagTimerManager {

    // таймер для мулти-флага, тяжело..
    private static class FlagTimer {

        Timer timer;
        int timeLeft;
        String flagName;
        MFlagPointCommand.FlagData flagData;
        Set<EntityPlayerMP> playersWithTimer = new HashSet<>();
    }

    private static final Map<String, FlagTimer> flagTimers = new HashMap<>();

    // функция для задания начала таймера
    public static void startFlagTimer(final String flagName, final int seconds,
        final List<EntityPlayerMP> initialPlayers, final int colorIndex) {
        // чтобы без наслаиваний больше
        stopFlagTimer(flagName, initialPlayers);
        // таймер показывается конкретного флага
        final MFlagPointCommand.FlagData flagData = getFlagData(flagName);
        if (flagData == null) return;
        // ооп больше ооп(создаю конструктор таймера флага)
        final FlagTimer flagTimer = new FlagTimer();
        flagTimer.timeLeft = seconds;
        flagTimer.flagName = flagName;
        flagTimer.flagData = new MFlagPointCommand.FlagData(
            flagData.name,
            flagData.x,
            flagData.y,
            flagData.z,
            colorIndex,
            flagData.flagplaced);
        flagTimers.put(flagName, flagTimer);
        // Создание нового таймера и его активация у конкретного флага
        flagTimer.timer = new Timer();
        flagTimer.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (flagTimer.timeLeft < 0) {
                    stopFlagTimer(flagName, initialPlayers);
                    String colorCode = FlagColors.getColorCode(colorIndex);
                    String colorName = FlagColors.getColorName(colorIndex);
                    CommandMod.network.sendToAll(
                        new PacketAnnouncement(
                            colorCode + colorName
                                + StatCollector.translateToLocal("message.flag.mtimer.flag_has_been_captured")
                                    .replace("{0}", flagName)));
                    for (EntityPlayerMP player : flagTimer.playersWithTimer) {
                        CommandMod.network
                            .sendTo(new PacketMultiFlagTimer(flagName, "", flagTimer.flagData.colorIndex), player);
                    }
                    return;
                }
                flagTimer.timeLeft--;
            }
        }, 0, 1000);
    }

    // функция сброса таймера
    public static void stopFlagTimer(String flagName, final List<EntityPlayerMP> initialPlayers) {
        FlagTimer flagTimer = flagTimers.remove(flagName);
        if (flagTimer != null && flagTimer.timer != null) {
            flagTimer.timer.cancel();
            // Сбросить таймер только игрокам в зоне
            for (EntityPlayerMP player : initialPlayers) {
                CommandMod.network
                    .sendTo(new PacketMultiFlagTimer(flagName, "", flagTimer.flagData.colorIndex), player);
            }
        }
    }

    // Вызывать на сервере раз в тик
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (FlagTimer flagTimer : flagTimers.values()) {
            Set<EntityPlayerMP> currentPlayers = FlagUtilities.getPlayersInHemisphere(
                DimensionManager.getWorld(0), // пока всё работает только для верхнего мира
                flagTimer.flagData,
                MFlagPointCommand.flagHemisphereRadius);
            // Показывает таймер игрокам вошедшим в зону
            for (EntityPlayerMP player : currentPlayers) {
                if (!flagTimer.playersWithTimer.contains(player)) {
                    CommandMod.network.sendTo(
                        new PacketMultiFlagTimer(
                            flagTimer.flagName,
                            formatTime(flagTimer.timeLeft),
                            flagTimer.flagData.colorIndex),
                        player);
                }
            }
            // Убирает таймер вышедшем игрокам
            for (EntityPlayerMP player : new HashSet<>(flagTimer.playersWithTimer)) {
                if (!currentPlayers.contains(player)) {
                    CommandMod.network.sendTo(
                        new PacketMultiFlagTimer(flagTimer.flagName, "", flagTimer.flagData.colorIndex),
                        player);
                }
            }
            flagTimer.playersWithTimer = currentPlayers;
            // Обновить таймер всем, кто остался
            for (EntityPlayerMP player : flagTimer.playersWithTimer) {
                CommandMod.network.sendTo(
                    new PacketMultiFlagTimer(
                        flagTimer.flagName,
                        formatTime(flagTimer.timeLeft),
                        flagTimer.flagData.colorIndex),
                    player);
            }
        }
    }

    // удобная функция для сбора даты из класса с командами
    private static MFlagPointCommand.FlagData getFlagData(String flagName) {
        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            if (flag.name.equals(flagName)) return flag;
        }
        return null;
    }

    // преоброзование по формату
    private static String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
