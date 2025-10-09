package com.myname.commandmodid;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketMultiFlagTimer implements IMessage {

    private String flagName;
    private String timerText;
    private int colorMeta;

    public PacketMultiFlagTimer(String flagName, String timerText, int colorMeta) {
        this.flagName = flagName;
        this.timerText = timerText;
        this.colorMeta = colorMeta;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        flagName = ByteBufUtils.readUTF8String(buf);
        timerText = ByteBufUtils.readUTF8String(buf);
        colorMeta = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, flagName);
        ByteBufUtils.writeUTF8String(buf, timerText);
        buf.writeInt(colorMeta);
    }

    public static class Handler implements IMessageHandler<PacketMultiFlagTimer, IMessage> {

        @Override
        public IMessage onMessage(final PacketMultiFlagTimer message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        TimerOverlay.setMultiFlagTimer(message.flagName, message.timerText);
                    }
                });
            return null;
        }
    }
}
