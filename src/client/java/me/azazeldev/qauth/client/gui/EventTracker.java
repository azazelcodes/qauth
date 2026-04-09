package me.azazeldev.qauth.client.gui;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class EventTracker {
    private static Map<String, Long> timers = new HashMap<>();
    private static boolean triedRefresh = false;
    public static void parse(String l, String target) {
        int ping = 0;//Minecraft.getInstance().player.connection.getPlayerInfo(Minecraft.getInstance().player.getStringUUID()).getLatency();
        double t = 0;
        for (String part : l.split(" ")) {
            double x = Double.parseDouble(part.substring(0,part.length()-1));
            switch (part) {
                case String p when p.contains("h"):
                    t += x*3600*1000;
                    break;
                case String p when p.contains("m"):
                    t += x*60*1000;
                    break;
                case String p when p.contains("s"):
                    t += x*1000;
                    break;
                default:
                    MainClient.sendClient(Component.literal("This cooldown has the improper format, not tracking!").withColor(0xFFFF0000));
                    return;
            }
        }
        triedRefresh = false;
        long n = Instant.now().toEpochMilli() + (long)(t) + ping;
        timers.put(target.replace(" cooldown:", ""), n);
    }
    public static void refresh() {
        if (StateManager.getState() == StateManager.AuthState.OFFLINE) return;
        if (triedRefresh) { // FIXME: this is ugly and temporary, make it refresh automatically on a 10min timer
            MainClient.sendClient(Component.literal("Tried refreshing timers already, try manually running /whenhideout or /whenradio in a bit!").withColor(0xFFFF0000));
            return;
        }
        triedRefresh = true;
        Minecraft.getInstance().player.connection.sendCommand("whenhideout");
        Minecraft.getInstance().player.connection.sendCommand("whenradio");
    }

    // HUD
    public static void EventPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (!Config.renderEvents) return;
        if (timers.isEmpty()) {
            refresh();
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        Font font = Minecraft.getInstance().font;
        int i = 0;
        for (Map.Entry<String, Long> e : timers.entrySet()) {
            long when = e.getValue() - Instant.now().toEpochMilli();
            if (when <= 0) refresh();
            Component c = Component.literal(e.getKey()+"in ").withStyle(ChatFormatting.AQUA).append(Component.literal(Instant.ofEpochMilli(when).toString().substring(11,19)).withColor(0xFFFFFFFF));
            int x = !Config.flipHUD ? window.getGuiScaledWidth() - font.width(c) -4 : 4;
            graphics.drawString(font, c, x, window.getGuiScaledHeight()-(4+i+1*font.lineHeight), 0xFFFFFFFF, true);
            i++;
        }
    }
}