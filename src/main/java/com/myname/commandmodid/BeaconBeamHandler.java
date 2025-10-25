package com.myname.commandmodid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BeaconBeamHandler {

    // Список мультифлагов
    public static final List<MFlagPointCommand.FlagData> clientFlags = new ArrayList<MFlagPointCommand.FlagData>();

    // Метод для отрисовки лучей в мире для всех установленных флагов
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.renderViewEntity;
        double px = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double py = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double pz = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        // Одиночный флаг
        if (FlagPointCommand.flagPointSet) {
            drawBeam(
                FlagPointCommand.flagPointX + 0.5 - px,
                FlagPointCommand.flagPointY - py,
                FlagPointCommand.flagPointZ + 0.5 - pz,
                BlockPlacementHandler.getFlagR(),
                BlockPlacementHandler.getFlagG(),
                BlockPlacementHandler.getFlagB());
        }

        // Мульти-флаги
        for (MFlagPointCommand.FlagData flag : clientFlags) {
            float[] color = FlagColors.getColor(flag.colorIndex);
            drawBeam(flag.x + 0.5 - px, flag.y - py, flag.z + 0.5 - pz, color[0], color[1], color[2]);
        }
    }

    // Универсальный метод для отрисовки луча, принимает координаты и цвет и
    // используем опенгаель функции для рисования
    private void drawBeam(double x, double y, double z, float r, float g, float b) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, 0.7F);

        int segments = 32;
        double radius = 0.3;
        double height = 75.0;
        // Рисуем цилиндрический луч с моим радиусом и высотой
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            GL11.glVertex3d(x + dx, y, z + dz);
            GL11.glVertex3d(x + dx, y + height, z + dz);
        }
        GL11.glEnd();
        // Несколько методов для правильной работы луча в OpenGL
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

    }
}
