package com.myname.commandmodid;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketMultiFlagTimer implements IMessage {

    private String flagName; // Название флага
    private String timerText; // Текст таймера
    private int colorMeta; // Цвет флага (meta-данные)

    // Конструктор по умолчанию (обязательно для десериализации)
    public PacketMultiFlagTimer() {}

    // Конструктор с аргументами (для инициализации данных)
    public PacketMultiFlagTimer(String flagName, String timerText, int colorMeta) {
        this.flagName = flagName;
        this.timerText = timerText;
        this.colorMeta = colorMeta;
    }

    // Метод для десериализации данных из ByteBuf
    @Override
    public void fromBytes(ByteBuf buf) {
        flagName = ByteBufUtils.readUTF8String(buf); // Читаем название флага
        timerText = ByteBufUtils.readUTF8String(buf); // Читаем текст таймера
        colorMeta = buf.readInt(); // Читаем цвет флага
    }

    // Метод для сериализации данных в ByteBuf
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, flagName); // Записываем название флага
        ByteBufUtils.writeUTF8String(buf, timerText); // Записываем текст таймера
        buf.writeInt(colorMeta); // Записываем цвет флага
    }

    // Обработчик пакета
    public static class Handler implements IMessageHandler<PacketMultiFlagTimer, IMessage> {

        @Override
        public IMessage onMessage(final PacketMultiFlagTimer message, MessageContext ctx) {
            // Выполняем обработку пакета на клиенте
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        // Устанавливаем таймер для флага на клиенте
                        TimerOverlay.setMultiFlagTimer(message.flagName, message.timerText);
                    }
                });
            return null; // Ответный пакет не требуется
        }
    }
}
