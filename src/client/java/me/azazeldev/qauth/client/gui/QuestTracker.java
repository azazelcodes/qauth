package me.azazeldev.qauth.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class QuestTracker extends Screen {

    //public static Item[] questi = {Items.ORANGE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE};
    public static Map<String, JsonObject> questIndices = new HashMap<>(); // <NPC, Indices> // FIXME: ugly
    public static boolean shouldOpenQT = false;

    public static void fetchQuest(String npc, String questName) {
        questIndices.put(npc, MainClient.fetchAPI("quests/"+npc+".index"));

        System.out.println(npc);
        System.out.println(questName);
        String index = questIndices.get(npc).get(questName).getAsString();
        if (index != null) {
            List<JsonObject> l = new ArrayList<>();
            if (Config.quests.containsKey(npc)) l.addAll(Config.quests.get(npc));
            l.add(MainClient.fetchAPI("quests/"+npc+"/"+index));
            Config.quests.put(npc, l);
        }
        else MainClient.sendClient(Component.literal("Your current quest was not found on the API, please message @im.azazel on Discord!").withColor(0xFFFF0000));

        Config.write(Main.MOD_ID);
    }

    public static void clear() {
        QuestTracker.questIndices.clear();
        Config.quests.clear();
        Config.write(Main.MOD_ID);
    }


    // GUI - I really tried using TabNavigationBar, it didnt fwiquin work after FIVE HOURS
    private final HeaderAndFooterLayout layout;
    private LinearLayout tabBar;
    private String npcTab;
    private Tab renderTab;
    private Integer questIndex;
    protected QuestTracker(String tab, Integer i) {
        super(Component.literal("qauth | Quest Tracker"));
        this.npcTab = tab;
        this.questIndex = i;
        this.layout = new HeaderAndFooterLayout(this, 13 + 9 + 3 + 15, 33);
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(3));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(this.getTitle(), this.font));


        int tabSize = this.width / Config.quests.keySet().size();
        this.tabBar = header.addChild(LinearLayout.horizontal().spacing(0));
        tabBar.newCellSettings().alignHorizontallyCenter();
        if (this.npcTab.equals("null")) this.npcTab = Config.quests.keySet().stream().toList().getFirst();
        for (String npc : Config.quests.keySet()) {
            Tab t = new Tab(MainClient.capitalize(npc), questIndex, tabSize);
            if (npc.equals(this.npcTab.toLowerCase())) this.renderTab = t;
        }

        if (this.renderTab != null) this.renderTab.render();


        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    private class Tab {
        String npc;
        Integer i;
        JsonObject quest;
        boolean multipleQ;
        public Tab(String npc, int i, int tabSize) {
            this.npc = npc;
            this.i = i;
            List<JsonObject> quests = Config.quests.get(this.npc.toLowerCase());
            this.multipleQ = quests.size() > 1;
            this.quest = quests.get(this.i % quests.size());
            QuestTracker.this.tabBar.addChild(Button.builder(
                    Component.literal(npc),
                    (Button button) -> setCurrent(0)
            ).width(tabSize).build());
        }

        public void render() { // FIXME: this is probably the ugliest thing Ive ever made
            LinearLayout tabContent = QuestTracker.this.layout.addToContents(LinearLayout.horizontal().spacing(8));

            if (multipleQ)
                tabContent.addChild(Button.builder(
                        Component.literal("<"),
                        (Button button) -> setCurrent(this.i-1)
                ).width(20).build());


            LinearLayout questContent = tabContent.addChild(LinearLayout.vertical().spacing(8));
            questContent.addChild(new StringWidget(Component.literal(quest.get("name").getAsString()), QuestTracker.this.font));

            questContent.addChild(new StringWidget(Component.literal("ᴄᴏɴᴅɪᴛɪᴏɴꜱ").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.DARK_GRAY), QuestTracker.this.font));
            for (Map.Entry<String, JsonElement> condition : quest.get("cond").getAsJsonObject().entrySet()) {
                questContent.addChild(new StringWidget(Component.literal(MainClient.capitalize(condition.getKey())).withStyle(ChatFormatting.GRAY).append(" ").append(Component.literal(stringifyCondition(condition)).withColor(0xFFFFFFFF)), QuestTracker.this.font));
            }

            questContent.addChild(new StringWidget(Component.literal("ʀᴇᴡᴀʀᴅꜱ").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.DARK_GRAY), QuestTracker.this.font));
            for (Map.Entry<String, JsonElement> reward : quest.get("rew").getAsJsonObject().entrySet()) { // TODO: add reward rendering
                //questContent.addChild(new StringWidget(, QuestTracker.this.font));
            }


            if (multipleQ)
                tabContent.addChild(Button.builder(
                        Component.literal(">"),
                        (Button button) -> setCurrent(this.i+1)
                ).width(20).build());

            tabContent.newCellSettings().alignHorizontallyCenter();
            tabContent.newCellSettings().alignVerticallyMiddle();
        }

        private void setCurrent(int i) {
            shouldOpenQT = true;
            QuestTracker.showGUI(String.format("quests_fetched %s %s", this.npc, i)); // FIXME: THIS IS SO UGLY, I JUST WANT TO GET THIS OVER WITH
        }
    }

    public static boolean showGUI(String cmd) {
        Screen s = Minecraft.getInstance().screen;
        if (s != null && !(s.getClass() == ChatScreen.class || s.getTitle().getString().toLowerCase().contains("profile") || s.getClass() == QuestTracker.class)) return true;
        if (!shouldOpenQT) Minecraft.getInstance().player.connection.sendCommand("profile"); // refetch quests on open
        shouldOpenQT = true;
        if (cmd.startsWith("quests_fetched")) { // FIXME: ugly workaround
            shouldOpenQT = false;
            String[] args = cmd.split(" ");
            Minecraft.getInstance().setScreen(new QuestTracker(args[1], Integer.parseInt(args[2])));
        }
        return true;
    }


    private static String stringifyCondition(Map.Entry<String, JsonElement> condition) {
        String s = "";
        switch (condition.getKey()) {
            case "kill", "collect", "hand_in":
                for (Map.Entry<String, JsonElement> k : condition.getValue().getAsJsonObject().entrySet()) s += k.getValue().getAsString() + " " + MainClient.capitalize(k.getKey()) + (k.getValue().getAsInt() > 1 ? "s" : "") + ", ";
                s = s.substring(0, s.length()-2);
                break;
            case "mark":
                for (JsonElement m : condition.getValue().getAsJsonArray()) s += MainClient.capitalize(m.getAsString()) + ", ";
                s = s.substring(0, s.length()-2);
                break;
            default: // TODO:
                return s;
        }
        return s;
    }
}
