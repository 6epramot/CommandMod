package com.myname.commandmodid;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketFlagBeam implements IMessage {

    public int x, y, z;
    public int colorIndex;
    public boolean enabled;

    public PacketFlagBeam() {}

    public PacketFlagBeam(int x, int y, int z, int colorIndex, boolean enabled) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.colorIndex = colorIndex;
        this.enabled = enabled;
    }

    // считывание
    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        colorIndex = buf.readInt();
        enabled = buf.readBoolean();

    }

    // сборка
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(colorIndex);
        buf.writeBoolean(enabled);
    }

    // менеджер пакетow
    public static class Handler implements IMessageHandler<PacketFlagBeam, IMessage> {

        @Override
        public IMessage onMessage(final PacketFlagBeam message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        if (message.enabled) {
                            FlagPointCommand.flagPointX = message.x;
                            FlagPointCommand.flagPointY = message.y;
                            FlagPointCommand.flagPointZ = message.z;
                            FlagPointCommand.flagPointSet = true;
                            BlockPlacementHandler.setFlagColorIndex(message.colorIndex);
                        } else {
                            FlagPointCommand.flagPointSet = false;
                        }
                    }
                });
            return null;
        }
    }
}
