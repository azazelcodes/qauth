package me.azazeldev.qauth.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.Config;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestTracker extends Screen {

    //public static Item[] questi = {Items.ORANGE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE};
    public static Map<String, JsonObject> questIndices = new HashMap<>(); // <NPC, Indices> // FIXME: ugly

    public static void fetchQuest(String npc, String questName) {
        questIndices.put(npc, MainClient.fetchAPI("quests/"+npc+".index"));

        System.out.println(npc);
        System.out.println(questName);
        JsonElement q = questIndices.get(npc).get(questName);
        String index = null;
        if (q != null) index = q.getAsString();
        if (index != null) {
            List<JsonObject> l = new ArrayList<>();
            if (Config.quests.containsKey(npc)) l.addAll(Config.quests.get(npc));
            l.add(MainClient.fetchAPI("quests/"+npc+"/"+index));
            Config.quests.put(npc, l);
        }
        else MainClient.sendClient("<red>Your current quest was not found on the API, please message @im.azazel on Discord!");
        Config.HANDLER.save();
    }

    public static void clear() {
        QuestTracker.questIndices.clear();
        Config.quests.clear();
        Config.HANDLER.save();
    }

    // HUD
    private static JsonObject trackedQuest;
    public static void QuestPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (!Config.renderQuest) return;
        if (trackedQuest == null) return;
        Window window = Minecraft.getInstance().getWindow();
        Font font = Minecraft.getInstance().font;
        int i = 0;
        for (Component c : componentifyQuest(trackedQuest)) { // FIXME: move this to an actual tracker using AttackMixin for kills, Inventory.contains() for hand_in, raid collection by tracking items in containermixin during raid state for collect, area checking with area api (WIP) and rc detection
            int x = !Config.flipHUD ? window.getGuiScaledWidth() - font.width(c) -4 : 4;
            graphics.drawString(font, c, x, 4+i*font.lineHeight, 0xFFFFFFFF, true);
            i++;
        }
    }



    // GUI - I really tried using TabNavigationBar, it didnt fwiquin work after FIVE HOURS
    public static boolean shouldOpenQT = false;

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
            for (Component c : QuestTracker.componentifyQuest(this.quest)) questContent.addChild(new StringWidget(c,QuestTracker.this.font));


            if (multipleQ)
                tabContent.addChild(Button.builder(
                        Component.literal(">"),
                        (Button button) -> setCurrent(this.i+1)
                ).width(20).build());

            tabContent.newCellSettings().alignHorizontallyCenter();
            tabContent.newCellSettings().alignVerticallyMiddle();

            LinearLayout footer = QuestTracker.this.layout.addToFooter(LinearLayout.horizontal().spacing(3));
            footer.defaultCellSetting().alignHorizontallyCenter();
            footer.addChild(Button.builder(
                    Component.literal("Track on HUD"),
                    (Button button) -> QuestTracker.trackedQuest = this.quest
            ).build());
        }

        private void setCurrent(int i) {
            shouldOpenQT = true;
            QuestTracker.showGUI(String.format("quests_fetched %s %s", this.npc, i)); // FIXME: THIS IS SO UGLY, I JUST WANT TO GET THIS OVER WITH
        }
    }

    public static boolean showGUI(String cmd) {
        Screen s = Minecraft.getInstance().screen;
        if (s != null && !(s.getClass() == ChatScreen.class || s.getTitle().getString().toLowerCase().contains("profile") || s.getClass() == QuestTracker.class)) return true;
        if (StateManager.getState() == StateManager.AuthState.LOBBY) if (!shouldOpenQT) Minecraft.getInstance().player.connection.sendCommand("profile"); // refetch quests on open
        else QuestTracker.showGUI(String.format("quests_fetched %s %s", MainClient.lastNPC, 0));
        shouldOpenQT = true;
        if (cmd.startsWith("quests_fetched")) { // FIXME: ugly workaround
            shouldOpenQT = false;
            String[] args = cmd.split(" ");
            Minecraft.getInstance().setScreen(new QuestTracker(args[1], Integer.parseInt(args[2])));
        }
        return true;
    }


    private static List<Component> componentifyQuest(JsonObject quest) {
        List<Component> components = new ArrayList<>();
        components.add(Component.literal(quest.get("name").getAsString()));

        components.add(MainClient.nativifyMiniMessage("<italic><underlined><dark_gray>ᴄᴏɴᴅɪᴛɪᴏɴꜱ"));
        for (Map.Entry<String, JsonElement> condition : quest.get("cond").getAsJsonObject().entrySet()) {
            components.add(Component.literal(MainClient.capitalize(condition.getKey())).withStyle(ChatFormatting.GRAY).append(" ").append(Component.literal(stringifyCondition(condition)).withColor(0xFFFFFFFF))); // todo: move to minimessage
        }

        components.add(MainClient.nativifyMiniMessage("<italic><underlined><dark_gray>ᴄᴏɴᴅɪᴛɪᴏɴꜱ"));
        for (Map.Entry<String, JsonElement> reward : quest.get("rew").getAsJsonObject().entrySet()) { // TODO: add reward rendering
            //components.add();
        }

        return components;
    }
    private static String stringifyCondition(Map.Entry<String, JsonElement> condition) {
        String s = "";
        switch (condition.getKey()) {
            case "kill", "collect", "hand_in":
                for (Map.Entry<String, JsonElement> k : condition.getValue().getAsJsonObject().entrySet()) s += k.getValue().getAsString() + " " + MainClient.capitalize(k.getKey()) + (k.getValue().getAsInt() > 1 ? "s" : "") + ", "; // minor spelling mistake for 3 bone mealS
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
