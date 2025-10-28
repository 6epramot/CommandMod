package com.myname.commandmodid.multiFlags;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

// отдельный класс для телепортации к мулти-флагам, чё нет то
public class TpFlagCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "tpflag";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tpflag <flag_name>";
    }

    // сама команда и её исполнение
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            throw new net.minecraft.command.WrongUsageException(getCommandUsage(sender));
        }
        if (sender instanceof EntityPlayerMP) {
            boolean ok = MFlagPointCommand.teleportToFlag((EntityPlayerMP) sender, args[0]);
            if (!ok) {
                sender.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("message.flag.tpcommand.flag_not_found")
                            .replace("{0}", args[0])));
            }
        }
    }
}
