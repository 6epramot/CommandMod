package com.myname.commandmodid.soloFlag;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.myname.commandmodid.CommandMod;
import com.myname.commandmodid.packets.PacketFlagBeam;
import com.myname.commandmodid.timers.PhaseActionBarTimer;

// класс команд для соло-флага
// надо будет как-нибудь добавить возможность удалять его
public class FlagPointCommand extends CommandBase {

    public static int flagHoldTimeMinutes = 5;
    public static int flagHoldTimeSeconds = 30;
    public static int preparationTimeMinutes = 0;
    public static int preparationTimeSeconds = 0;
    public static int flagPointX = 0;
    public static int flagPointY = 0;
    public static int flagPointZ = 0;
    public static boolean flagPointSet = false;
    public static boolean preparationPhase = false;

    @Override
    public String getCommandName() {
        return "flagpoint";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.flagpoint.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    // обработчик доп команд, нужно будет сделать так чтобы на таб выдавал варианты
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new WrongUsageException(getCommandUsage(sender));

        if ("time".equalsIgnoreCase(args[0])) {
            handleTimeCommand(sender, args);
            return;
        }
        if ("prepare".equalsIgnoreCase(args[0])) {
            handlePrepareCommand(sender, args);
            return;
        }
        if (args.length >= 3 && isInteger(args[0]) && isInteger(args[1]) && isInteger(args[2])) {
            handleFlagPointCommand(sender, args);
            return;
        }
        throw new WrongUsageException(getCommandUsage(sender));
    }

    // функция для основной команды установки точки флага
    private void handleFlagPointCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            flagPointX = parseInt(sender, args[0]);
            flagPointY = parseInt(sender, args[1]);
            flagPointZ = parseInt(sender, args[2]);

            if (flagPointY < 0 || flagPointY > 255) {
                throw new CommandException("commands.flagpoint.invalidY");
            }
            flagPointSet = true;
            CommandMod.network.sendToAll(new PacketFlagBeam(flagPointX, flagPointY, flagPointZ, 0, true));
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "Flag point set to: "
                        + flagPointX
                        + ", "
                        + flagPointY
                        + ", "
                        + flagPointZ));

        } catch (NumberFormatException e) {
            throw new WrongUsageException("commands.generic.num.invalid", args[0]);
        }
    }

    // фукнция для установки времени удержания флага
    private void handleTimeCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) throw new WrongUsageException("commands.flagpoint.time.usage");
        int min = parseAndValidateTime(sender, args[1], args[2], "commands.flagpoint.time.invalid");
        flagHoldTimeMinutes = min;
        flagHoldTimeSeconds = Integer.parseInt(args[2]);
        sender.addChatMessage(
            new ChatComponentText(
                EnumChatFormatting.GREEN + "Flag hold time set to: "
                    + flagHoldTimeMinutes
                    + " minutes "
                    + flagHoldTimeSeconds
                    + " seconds"));

    }

    // функция для установки времени подготовки перед установкой флага, надо будет
    // объединить с мультифлагом или там дублировать хз
    public void handlePrepareCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) throw new WrongUsageException("commands.flagpoint.prepare.usage");
        int min = parseAndValidateTime(sender, args[1], args[2], "commands.flagpoint.prepare.invalid");
        preparationTimeMinutes = min;
        preparationTimeSeconds = Integer.parseInt(args[2]);
        preparationPhase = true;

        PhaseActionBarTimer.start();
        sender.addChatMessage(
            new ChatComponentText(
                EnumChatFormatting.GREEN + "Preparation time set to: "
                    + preparationTimeMinutes
                    + " minutes "
                    + preparationTimeSeconds
                    + " seconds"));
    }

    // простая функция для преоброзования тиков в секунды и минуты с проверкой на
    // валидность
    private int parseAndValidateTime(ICommandSender sender, String minArg, String secArg, String errorKey)
        throws CommandException {
        int min, sec;
        try {
            min = parseInt(sender, minArg);
            sec = parseInt(sender, secArg);
        } catch (NumberFormatException e) {
            throw new WrongUsageException("commands.generic.num.invalid", minArg);
        }
        if (min < 0 || sec < 0 || sec > 59) {
            throw new CommandException(errorKey);
        }
        return min;
    }

    // табуляция для команд
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "time", "prepare");
        }
        return null;
    }

    // проверка на то число это или нет
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Геттеры для доступа к координатам и времени из других классов
    public static int getFlagPointX() {
        return flagPointX;
    }

    public static int getFlagPointY() {
        return flagPointY;
    }

    public static int getFlagPointZ() {
        return flagPointZ;
    }

    public static boolean isFlagPointSet() {
        return flagPointSet;
    }

    public static void resetFlagPointSet() {
        flagPointSet = false;
    }

    public static void resetPrepareTime() {
        flagPointSet = false;
    }
}
