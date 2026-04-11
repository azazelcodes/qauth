package me.azazeldev.qauth.client.gui;

import com.mojang.blaze3d.platform.Window;
import me.azazeldev.qauth.client.Compatibility;
import me.azazeldev.qauth.client.Config;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        if (true) return;
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
                    MainClient.sendClient("<red>This cooldown has the improper format, not tracking!");
                    return;
            }
        }
        triedRefresh = false;
        long n = Instant.now().toEpochMilli() + (long)(t) + ping;
        timers.put(target.replace(" cooldown:", ""), n);
    }
    public static void refresh() {
        if (true) return; // FIXME: this kicks for spamming because of the render loop. put this on a different thread?
        /* // TODO: fix
         * wait until a message related to event trigger passes
         * this could be:
         * - hideout: "&kXXX s hideout is unlocking" ?
         * - radio: "! The virtue building radio control is locking!, gas will fill the room in 3 seconds."
         */
        if (StateManager.getState() == StateManager.AuthState.OFFLINE) return;
        if (triedRefresh) { // FIXME: this is ugly and temporary, make it refresh automatically on a 10min timer
            MainClient.sendClient("<red>Tried refreshing timers already, try manually running <dark_red>/whenhideout</dark_red> or <dark_red>/whenradio</dark_red> in a bit!");
            return;
        }
        triedRefresh = true;
        Minecraft.getInstance().player.connection.sendCommand("whenhideout");
        Minecraft.getInstance().player.connection.sendCommand("whenradio");
    }

    // HUD
    public static void EventPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (true) return;
        if (!Config.renderEvents) return;
        if (timers.isEmpty()) {
            refresh();
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        Font font = Minecraft.getInstance().font;

        graphics.pose().pushMatrix();
        Compatibility.translate(graphics, "events");
        int i = 0;
        for (Map.Entry<String, Long> e : timers.entrySet()) {
            long when = e.getValue() - Instant.now().toEpochMilli();
            if (when <= 0) refresh();
            Component c = MainClient.nativifyKyori(MiniMessage.miniMessage().deserialize("<aqua><event>in <white><time>", Placeholder.unparsed("event",e.getKey()), Placeholder.unparsed("time",Instant.ofEpochMilli(when).toString().substring(11,19))));
            int x = !Config.flipHUD ? window.getGuiScaledWidth() - font.width(c) -4 : 4;
            graphics.drawString(font, c, x, window.getGuiScaledHeight()-(4+i*font.lineHeight), 0xFFFFFFFF, true);
            i++;
        }
        graphics.pose().popMatrix();
    }
}