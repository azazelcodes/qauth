package me.azazeldev.qauth.client.gui;

import com.google.gson.JsonObject;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QuestTracker {

    //public static Item[] questi = {Items.ORANGE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE};
    public static Map<String, JsonObject> questIndices = new HashMap<>(); // <NPC, Indices> // FIXME: ugly

    public static void fetchQuest(String npc, String questName) {
        Config.quests.remove(npc);
        questIndices.put(npc, MainClient.fetchAPI("quests/"+npc+".index"));

        String index = questIndices.get(npc).get(questName).getAsString();
        if (index != null) Config.quests.put(npc, MainClient.fetchAPI("quests/"+npc+"/"+index));
        else MainClient.sendClient(Component.literal("Your current quest was not found on the API, please message @im.azazel on Discord!").withColor(0xFFFF0000));

        MainClient.sendClient(Component.literal("Fetched? "+index));
        Config.write(Main.MOD_ID);
    }


    // GUI


    /*public static boolean showGUI(String cmd) {
        Screen s = Minecraft.getInstance().screen;
        if (s != null && s.getClass() != ChatScreen.class) return true;
        Minecraft.getInstance().setScreen(new QuestTracker());
        return true;
    }*/
}
