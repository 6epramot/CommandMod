package com.myname.commandmodid;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class FlagListCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "mflaglist";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mflaglist";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (MFlagPointCommand.getAllFlags()
            .isEmpty()) {
            sender.addChatMessage(new ChatComponentText("§7Нет установленных мульти-флагов."));
            return;
        }
        sender.addChatMessage(new ChatComponentText("§6Список флагов:"));
        for (MFlagPointCommand.FlagData flag : MFlagPointCommand.getAllFlags()) {
            String msg = String.format("§e%s§r | §b%d§r | §b%d§r | §b%d§r |", flag.name, flag.x, flag.y, flag.z);
            sender.addChatMessage(new ChatComponentText(msg));
        }
    }
}
