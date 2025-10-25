package com.myname.commandmodid;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

// построение принятие и отправка пакетов для сообщений конкретному игроку
public class PacketPersonalMessage implements IMessage {

    public String text = "";

    // конструктор по классике
    public PacketPersonalMessage() {}

    public PacketPersonalMessage(String text) {
        this.text = text != null ? text : "";
    }

    // мистер сборщик
    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    // мистер дессириализатор
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

    // мюсье пакет менеджер
    public static class Handler implements IMessageHandler<PacketPersonalMessage, IMessage> {

        @Override
        public IMessage onMessage(final PacketPersonalMessage message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        TimerOverlay.addPersonalMessage(message.text);
                    }
                });
            return null;
        }
    }
}
