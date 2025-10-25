package com.myname.commandmodid;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

// сетевые пакеты для глобальных сообщений всем игрокам
public class PacketAnnouncement implements IMessage {

    public String text = "";

    public PacketAnnouncement() {}

    public PacketAnnouncement(String text) {
        this.text = text != null ? text : "";
    }

    // сборка
    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    // считывание
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

    // менеджер пакетов
    public static class Handler implements IMessageHandler<PacketAnnouncement, IMessage> {

        @Override
        public IMessage onMessage(final PacketAnnouncement message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        TimerOverlay.addAnnouncement(message.text);
                    }
                });
            return null;
        }
    }
}
