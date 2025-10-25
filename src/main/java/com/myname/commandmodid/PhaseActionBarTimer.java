package com.myname.commandmodid;

import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class PhaseActionBarTimer {

    private static Timer timer;

    /**
     * Запуск таймера для текущей фазы (подготовка или захват флага)
     */
    public static void start() {
        // Останавливаем предыдущий таймер, если был
        stop();

        int totalSeconds;
        String phaseName;

        if (FlagPointCommand.preparationPhase) {
            totalSeconds = FlagPointCommand.preparationTimeMinutes * 60 + FlagPointCommand.preparationTimeSeconds;
            phaseName = StatCollector.translateToLocal("message.flag.bartimer.name_prepare");
        } else if (FlagPointCommand.isFlagPointSet()) {
            totalSeconds = FlagPointCommand.flagHoldTimeMinutes * 60 + FlagPointCommand.flagHoldTimeSeconds;
            phaseName = StatCollector.translateToLocal("message.flag.bartimer.name_capture_flag");
        } else {
            return;
        }

        // Запускаем только на серваке
        if (FMLCommonHandler.instance()
            .getEffectiveSide() != Side.SERVER) return;

        final String phaseNameFinal = phaseName;
        final int[] secondsLeft = { totalSeconds };

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (secondsLeft[0] < 0) {
                    stop();
                    if (FlagPointCommand.preparationPhase) {
                        FlagPointCommand.preparationPhase = false;
                        CommandMod.network.sendToAll(
                            new PacketAnnouncement(
                                StatCollector.translateToLocal("message.flag.bartimer.prepare_end")));
                    } else {
                        FlagVictoryHandler.checkVictory();
                    }
                    // Отправляем пустой таймер для очистки у всех клиентов
                    if (!MinecraftServer.getServer()
                        .getConfigurationManager().playerEntityList.isEmpty()) {
                        CommandMod.network.sendToAll(new PacketTimerText(""));
                    }
                    return;
                }

                // Отправляем обновление таймера всем клиентам
                if (!MinecraftServer.getServer()
                    .getConfigurationManager().playerEntityList.isEmpty()) {
                    String text = phaseNameFinal + ": " + formatTime(secondsLeft[0]);
                    CommandMod.network.sendToAll(new PacketTimerText(text));
                }
                secondsLeft[0]--;
            }
        }, 0, 1000);
    }

    /**
     * Остановка таймера (без очистки overlay на клиенте — это делает сервер через
     * пакет)
     */
    public static void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        // Очищаем текст таймера на клиенте
    }

    private static String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
