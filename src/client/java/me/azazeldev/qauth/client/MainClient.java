package me.azazeldev.qauth.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.gui.QuestTracker;
import me.azazeldev.qauth.client.gui.StashTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.lang3.ArrayUtils.contains;

public class MainClient implements ClientModInitializer {

    private static String[] ips = {"unauth.xyz","130.12.33.16"};
    public static boolean onauth = false;

    public static KeyMapping markValuable;

    public static String lastNPC;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category MAIN_CATEGORY = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(Main.MOD_ID, "qauth")
        );
        markValuable = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.qauth.mark_valuable",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        MAIN_CATEGORY
                )
        );

        CommandHandler.popCmds();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> CommandHandler.registerBrigadier(dispatcher));

        ClientTickEvents.START_WORLD_TICK.register((tick) -> {
            @Nullable ServerData server = Minecraft.getInstance().getCurrentServer();
            if (server == null) return;
            if (Minecraft.getInstance().isSingleplayer()) return;
            if (contains(ips, server.ip)) onauth = true;
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath(Main.MOD_ID, "stash_preview"), StashTracker::StashPreview); // FIXME: draw above all screens, even containers - how? idk
        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath(Main.MOD_ID, "quest_preview"), QuestTracker::QuestPreview);
    }

    public static void sendClient(Component msg) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("qa>>").withStyle(ChatFormatting.ITALIC).withColor(0xC889CFF0).append(Component.literal(" ").withStyle(ChatFormatting.RESET).append(msg))); // TODO: move to minimessage
    }

    public static JsonObject fetchAPI(String path) { // FIXME: faster plz
        /* old way of downloading quest index, this stores the filename as the sha1
        HttpUtil.DownloadProgressListener listener = new HttpUtil.DownloadProgressListener() {
            @Override
            public void requestStart() {}

            @Override
            public void downloadStart(OptionalLong size) {}

            @Override
            public void downloadedBytes(long l) {}

            @Override
            public void requestFinished(boolean success) {
                System.out.println("Done: " + success);
            }
        };
        try {
            HttpUtil.downloadFile(
                    FabricLoader.getInstance().getConfigDir().resolve("qa"),
                    URI.create("https://raw.githubusercontent.com/azazelcodes/uaapi/refs/heads/master/quests/therapist.index.json").toURL(),
                    new HashMap<>(),
                    Hashing.sha1(),
                    null,
                    64 * 1024 * 1024, // 64mb?
                    Proxy.NO_PROXY,
                    listener
            );
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL :(");
        }
        */
        try {
            URL url = URI.create(
                    "https://raw.githubusercontent.com/azazelcodes/uaapi/master/" + path + ".json" // FIXME: String.format
            ).toURL();
            try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch(IOException e) {
            // FIXME: catch
        }
        return null;
    }

    public static String capitalize(String s) { String ws = ""; for (String w : s.split("_")) ws += w.substring(0, 1).toUpperCase() + w.substring(1) + " "; return ws.substring(0,ws.length()-1); }
}
