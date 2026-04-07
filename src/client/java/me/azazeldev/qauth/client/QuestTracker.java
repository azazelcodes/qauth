package me.azazeldev.qauth.client;

import com.google.gson.JsonObject;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

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
}
