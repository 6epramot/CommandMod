package com.myname.commandmodid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketAllFlags implements IMessage {

    public List<MFlagPointCommand.FlagData> flagList = new ArrayList<MFlagPointCommand.FlagData>();

    public PacketAllFlags() {}

    public PacketAllFlags(Collection<MFlagPointCommand.FlagData> flags) {
        flagList.addAll(flags);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            int color = buf.readInt();
            boolean flagplaced = buf.readBoolean();
            flagList.add(new MFlagPointCommand.FlagData(name, x, y, z, color, flagplaced));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(flagList.size());
        for (MFlagPointCommand.FlagData flag : flagList) {
            ByteBufUtils.writeUTF8String(buf, flag.name);
            buf.writeInt(flag.x);
            buf.writeInt(flag.y);
            buf.writeInt(flag.z);
            buf.writeInt(flag.colorIndex);
            buf.writeBoolean(flag.flagplaced);
        }
    }

    public static class Handler implements IMessageHandler<PacketAllFlags, IMessage> {

        @Override
        public IMessage onMessage(final PacketAllFlags message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        BeaconBeamHandler.clientFlags.clear();
                        BeaconBeamHandler.clientFlags.addAll(message.flagList);
                    }
                });
            return null;
        }
    }
}
