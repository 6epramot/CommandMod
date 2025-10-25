package com.myname.commandmodid;

import net.minecraft.util.StatCollector;

// класс откуда беру цвте
public class FlagColors {

    public static final float[][] COLORS = { { 1.0F, 1.0F, 1.0F }, // белый
        { 1.0F, 0.5F, 0.0F }, // оранжевый
        { 1.0F, 0.0F, 1.0F }, // пурпурный
        { 0.5F, 0.5F, 1.0F }, // голубой
        { 1.0F, 1.0F, 0.0F }, // жёлтый
        { 0.5F, 1.0F, 0.0F }, // лаймовый
        { 1.0F, 0.0F, 0.5F }, // розовый
        { 0.3F, 0.3F, 0.3F }, // серый
        { 0.6F, 0.6F, 0.6F }, // светло-серый
        { 0.0F, 0.0F, 1.0F }, // синий
        { 0.5F, 0.0F, 0.5F }, // фиолетовый
        { 0.0F, 0.0F, 0.5F }, // тёмно-синий
        { 0.5F, 0.25F, 0.0F }, // коричневый
        { 0.0F, 0.5F, 0.0F }, // зелёный
        { 1.0F, 0.0F, 0.0F }, // красный
        { 0.0F, 0.0F, 0.0F } // чёрный
    };
    // надо ланги
    // получаем по индексу имя цвета и выдаём сообщение в соответствии ему
    public static final String[] COLOR_NAMES = { StatCollector.translateToLocal("message.flag.color.white"),
        StatCollector.translateToLocal("message.flag.color.orange"),
        StatCollector.translateToLocal("message.flag.color.magenta"),
        StatCollector.translateToLocal("message.flag.color.light_blue"),
        StatCollector.translateToLocal("message.flag.color.yellow"),
        StatCollector.translateToLocal("message.flag.color.lime"),
        StatCollector.translateToLocal("message.flag.color.pink"),
        StatCollector.translateToLocal("message.flag.color.gray"),
        StatCollector.translateToLocal("message.flag.color.light_gray"),
        StatCollector.translateToLocal("message.flag.color.cyan"),
        StatCollector.translateToLocal("message.flag.color.purple"),
        StatCollector.translateToLocal("message.flag.color.blue"),
        StatCollector.translateToLocal("message.flag.color.brown"),
        StatCollector.translateToLocal("message.flag.color.green"),
        StatCollector.translateToLocal("message.flag.color.red"),
        StatCollector.translateToLocal("message.flag.color.black") };

    public static final String[] COLOR_CODES = { "§f", // белая
        "§6", // оранжевая
        "§d", // пурпурная
        "§b", // голубая
        "§e", // желтая
        "§a", // лаймовая
        "§d", // розовая
        "§8", // серая
        "§7", // светло-серая
        "§3", // бирюзовая
        "§5", // фиолетовая
        "§9", // синяя
        "§6", // коричневая
        "§2", // зеленая
        "§c", // красная
        "§0" // черная
    };

    public static float[] getColor(int idx) {
        if (idx >= 0 && idx < COLORS.length) return COLORS[idx];
        return COLORS[0];
    }

    public static String getColorName(int idx) {
        if (idx >= 0 && idx < COLOR_NAMES.length) return COLOR_NAMES[idx];
        return StatCollector.translateToLocal("message.flag.color.unknown");
    }

    public static String getColorCode(int idx) {
        if (idx >= 0 && idx < COLOR_CODES.length) return COLOR_CODES[idx];
        return "§f";
    }
}
