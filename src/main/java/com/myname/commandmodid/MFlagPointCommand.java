package com.myname.commandmodid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class MFlagPointCommand extends CommandBase {

    // навалил ООП
    public static class FlagData {

        public final String name;
        public final int x, y, z;
        public final int colorIndex;
        public boolean flagplaced;

        public FlagData(String name, int x, int y, int z, int colorIndex, boolean flagplaced) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.colorIndex = colorIndex;
            this.flagplaced = flagplaced;
        }
    }

    public static int flagCaptureTime = 60;
    public static double flagHemisphereRadius = 5.0;
    private static final Map<String, FlagData> flags = new HashMap<>();

    @Override
    public String getCommandName() {
        return "mflagpoint";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mflagpoint <name> <x> <y> <z> [color] | /mflagpoint <name> delete | /mflagpoint <old_name> <new_name>";
    }

    @Override
    // команды для мультифлага, реализовал без функций,
    // мб потом структурирую по красоте, но сначала ланги
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("radius")) {
            try {
                double newRadius = Double.parseDouble(args[1]);
                if (newRadius < 1.0 || newRadius > 500.0) {
                    sender.addChatMessage(
                        new ChatComponentText(StatCollector.translateToLocal("message.flag.mflag.invalid_radius")));
                    return;
                }
                flagHemisphereRadius = newRadius;
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.mflag.radius_added")
                            .replace("{0}", Double.toString(newRadius))));
            } catch (NumberFormatException e) {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.invalid_number")
                            .replace("{0}", args[1])));
            }
            return;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("capturetime")) {
            try {
                int newTime = Integer.parseInt(args[1]);
                if (newTime < 1 || newTime > 3600) {
                    sender.addChatMessage(
                        new ChatComponentText(
                            StatCollector.translateToLocal("message.flag.mflag.invalid_capture_time")));
                    return;
                }
                flagCaptureTime = newTime;
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.mflag.invalid_capture_time")
                            .replace("{0}", Integer.toString(newTime))));
            } catch (NumberFormatException e) {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.invalid_number")
                            .replace("{0}", args[1])));
            }
            return;
        }
        if (args.length >= 4) {
            // /mflagpoint <name> <x> <y> <z> [color]
            String name = args[0];
            int x = parseInt(sender, args[1]);
            int y = parseInt(sender, args[2]);
            int z = parseInt(sender, args[3]);
            int colorIndex = 0;
            if (args.length >= 5) {
                colorIndex = parseInt(sender, args[4]);
            }

            // Проверка на совпадение координат
            String toRemove = null;
            for (Map.Entry<String, FlagData> entry : flags.entrySet()) {
                FlagData data = entry.getValue();
                if (data.x == x && data.y == y && data.z == z) {
                    toRemove = entry.getKey();
                    break;
                }
            }
            if (toRemove != null) {
                flags.remove(toRemove);
            }

            // Проверка на совпадение имени
            String finalName = name;
            int copy = 1;
            while (flags.containsKey(finalName)) {
                finalName = name + "_" + copy;
                copy++;
            }

            FlagData flag = new FlagData(finalName, x, y, z, colorIndex, false);
            flags.put(finalName, flag);

            sender.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("message.flag.mflag.admin_message_set")
                        .replace("{0}", finalName)
                        .replace("{1}", Integer.toString(x))
                        .replace("{2}", Integer.toString(y))
                        .replace("{3}", Integer.toString(z))
                        .replace("{4}", Integer.toString(colorIndex))));
            syncFlagsToAll();
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("delete")) {
            // /mflagpoint <name> delete
            String name = args[0];
            FlagData removed = flags.remove(name);
            if (removed != null) {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.mflag.delete")
                            .replace("{0}", name)));
                syncFlagsToAll();
            } else {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.mflag.not_found")
                            .replace("{0}", name)));
            }
            return;
        }

        if (args.length == 2) {
            // /mflagpoint <old_name> <new_name>
            String oldName = args[0];
            String newName = args[1];
            if (!flags.containsKey(oldName)) {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.mflag.not_found")
                            .replace("{0}", oldName)));
                return;
            }
            if (flags.containsKey(newName)) {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.mflag.already_existed")
                            .replace("{0}", newName)));
                return;
            }
            FlagData data = flags.remove(oldName);
            FlagData renamed = new FlagData(newName, data.x, data.y, data.z, data.colorIndex, data.flagplaced);
            flags.put(newName, renamed);
            sender.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("message.flag.mflag.renamed")
                        .replace("{0}", oldName)
                        .replace("{0}", newName)));
            syncFlagsToAll();
            return;
        }

        throw new WrongUsageException(getCommandUsage(sender));
    }

    // функция для телепортации к флагу
    public static boolean teleportToFlag(EntityPlayerMP player, String flagName) {
        FlagData data = flags.get(flagName);
        if (data != null) {
            player.setPositionAndUpdate(data.x + 0.5, data.y, data.z + 0.5);
            return true;
        }
        return false;
    }

    public static void setFlagColor(String flagName, int colorIndex) {
        FlagData old = flags.get(flagName);
        if (old != null) {
            flags.put(flagName, new FlagData(old.name, old.x, old.y, old.z, colorIndex, old.flagplaced));
            // Синхронизируем флаги с клиентами
            CommandMod.network.sendToAll(new PacketAllFlags(getAllFlags()));
        }
    }

    public static Set<String> getFlagNames() {
        return flags.keySet();
    }

    public static Collection<FlagData> getAllFlags() {
        return flags.values();
    }

    private void syncFlagsToAll() {
        CommandMod.network.sendToAll(new PacketAllFlags(getAllFlags()));
    }
}
