package com.myname.commandmodid;

import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class TimerOverlay {

    public static String timerText = "";
    public static final Queue<String> announcements = new LinkedList<>();
    public static final Queue<String> personalMessages = new LinkedList<>();
    private static long announcementTimestamp = 0;
    private static long personalMessageTimestamp = 0;
    private static final long ANNOUNCEMENT_DURATION = 3000;
    private static final long PERSONAL_DURATION = 2000;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        long now = System.currentTimeMillis();

        // Таймер по центру, крупно
        if (timerText != null && !timerText.isEmpty()) {
            GL11.glPushMatrix();
            GL11.glScalef(2.0F, 2.0F, 2.0F);
            int timerWidth = mc.fontRenderer.getStringWidth(timerText);
            mc.fontRenderer.drawStringWithShadow(
                timerText,
                (int) ((width / 2 - timerWidth / 2) / 2.0F),
                (int) (10 / 2.0F),
                0xFFFF55);
            GL11.glPopMatrix();
        }

        // Последнее объявление по центру, крупно, исчезает через ANNOUNCEMENT_DURATION
        String lastAnnouncement = announcements.peek();
        if (lastAnnouncement != null && !lastAnnouncement.isEmpty()) {
            if (now - announcementTimestamp < ANNOUNCEMENT_DURATION) {
                GL11.glPushMatrix();
                GL11.glScalef(2.0F, 2.0F, 2.0F);
                int msgWidth = mc.fontRenderer.getStringWidth(lastAnnouncement);
                mc.fontRenderer.drawStringWithShadow(
                    lastAnnouncement,
                    (int) ((width / 2 - msgWidth / 2) / 2.0F),
                    (int) (40 / 2.0F),
                    0xFFFFFF);
                GL11.glPopMatrix();
            } else {
                announcements.clear();
            }
        }

        // Личные сообщения над полоской опыта
        int expBarY = height - 32;
        int msgY = expBarY - personalMessages.size() * 12;
        if (!personalMessages.isEmpty()) {
            if (now - personalMessageTimestamp < PERSONAL_DURATION) {
                for (String msg : personalMessages) {
                    mc.fontRenderer
                        .drawStringWithShadow(msg, width / 2 - mc.fontRenderer.getStringWidth(msg) / 2, msgY, 0xFFFFFF);
                    msgY += 12;
                }
            } else {
                personalMessages.clear();
            }
        }
    }

    // Добавить объявление (только на клиенте, вызывается обработчиком пакета)
    public static void addAnnouncement(String msg) {
        announcements.clear();
        announcements.offer(msg);
        announcementTimestamp = System.currentTimeMillis();
    }

    // Добавить личное сообщение (только на клиенте)
    public static void addPersonalMessage(String msg) {
        if (personalMessages.size() > 3) personalMessages.poll();
        personalMessages.offer(msg);
        personalMessageTimestamp = System.currentTimeMillis();
    }
}
