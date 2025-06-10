package com.myname.commandmodid;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketTimerText implements IMessage {

    public String text = "";

    public PacketTimerText() {}

    public PacketTimerText(String text) {
        this.text = text != null ? text : "";
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int len = buf.readInt();
        if (len > 0) {
            byte[] bytes = new byte[len];
            buf.readBytes(bytes);
            text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } else {
            text = "";
        }
    }

    public static class Handler implements IMessageHandler<PacketTimerText, IMessage> {

        @Override
        public IMessage onMessage(final PacketTimerText message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        com.myname.commandmodid.TimerOverlay.timerText = message.text;
                    }
                });
            return null;
        }
    }
}
