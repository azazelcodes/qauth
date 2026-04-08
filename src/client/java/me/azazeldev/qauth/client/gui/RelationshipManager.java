package me.azazeldev.qauth.client.gui;

import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RelationshipManager extends Screen { // disabled due to katherines request
    protected HeaderAndFooterLayout layout;
    protected LinearLayout scrollContainer;
    protected LinearLayout searchContainer;
    private RelationshipManager.PlayerList plist;
    private RelationshipManager.TagList tlist;
    private LinearLayout buttons;
    public int lwidth;

    private List<String> players;
    private String selectedPlayer;
    private String selectedTag;
    private String targetTag;
    private int color = 0xFFFFFFFF;

    protected RelationshipManager() {
        super(Component.literal("qauth | Relationship Manager"));
        this.layout = new HeaderAndFooterLayout(this, 13 + 9 + 3 + 15, 33);
        if (Minecraft.getInstance().getCurrentServer() != null) this.players = this.minecraft.player.connection.getOnlinePlayers().stream().map(playerInfo -> playerInfo.getProfile().name()).toList();
    }

    @Override
    protected void init() {
        super.init();
        this.lwidth = this.width / 3;
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(3));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(this.getTitle(), this.font));

        this.scrollContainer = this.layout.addToContents(LinearLayout.horizontal().spacing(this.lwidth));
        this.searchContainer = this.layout.addToHeader(LinearLayout.horizontal().spacing(this.lwidth));

        // player list
        EditBox psearch = this.searchContainer.addChild(new EditBox(this.font, this.lwidth, 15, Component.empty()));
        RelationshipManager.PlayerList playerList = new RelationshipManager.PlayerList();
        psearch.setHint(Component.literal("Username"));
        psearch.setResponder(playerList::filterEntries);
        this.plist = this.scrollContainer.addChild(playerList);
        this.plist.setSelected( null );

        // buttons
        /* FIXME: add buttons here to:
         * - add tag modifiers?
         */
        this.buttons = this.layout.addToContents(LinearLayout.vertical().spacing(3));
        buttons.newCellSettings().alignHorizontallyCenter();
        this.buttons.addChild(Button.builder(
                Component.literal("Apply"),
                (Button button) -> {
                    if (this.selectedPlayer != null && this.selectedTag != null)
                        if (this.selectedTag.equals("none")) Config.relations.remove(this.selectedPlayer.toLowerCase());
                        else Config.relations.put(this.selectedPlayer.toLowerCase(), this.selectedTag);
                    Config.write(Main.MOD_ID);
                }
        ).build());

        EditBox targetTag = this.buttons.addChild(new EditBox(this.font, 150, 15, Component.empty()));
        targetTag.setHint(Component.literal("Relationship Tag"));
        targetTag.setResponder((s) -> this.targetTag = s);


        EditBox colTag = this.buttons.addChild(new EditBox(this.font, 150, 15, Component.empty()));
        colTag.setHint(Component.literal("Color"));
        colTag.setResponder((s) -> {
            if (s.length() == 6) this.color = 0xFF000000 | (int)Long.parseLong(s, 16);
            colTag.setTextColor(this.color);
        });

        this.buttons.addChild(Button.builder(
                Component.literal("Create"),
                (Button button) -> {
                    if (this.targetTag != null && !this.targetTag.equals("none"))
                        Config.tags.put(this.targetTag, this.color);
                    updatePos(); // this is needed to refresh the list
                    tlist.filterEntries("");
                    Config.write(Main.MOD_ID);
                }
        ).build());
        this.buttons.addChild(Button.builder(
                Component.literal("Delete"),
                (Button button) -> {
                    if (this.targetTag != null)
                        Config.tags.remove(this.targetTag);
                    updatePos(); // this is needed to refresh the list
                    tlist.filterEntries("");
                    Config.write(Main.MOD_ID);
                }
        ).build());

        // tag list
        EditBox tsearch = this.searchContainer.addChild(new EditBox(this.font, this.lwidth, 15, Component.empty())); // TODO: probably dont need this
        RelationshipManager.TagList tagList = new RelationshipManager.TagList();
        tsearch.setHint(Component.literal("Relationship Tag"));
        tsearch.setResponder(tagList::filterEntries);
        this.tlist = this.scrollContainer.addChild(tagList);
        this.tlist.setSelected( null );

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }
    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());

        // FIXME: this trash breaks on window resize??
        updatePos();
    }


    private class PlayerList extends ObjectSelectionList<RelationshipManager.PlayerList.Entry> {
        private PlayerList() {
            super(
                    RelationshipManager.this.minecraft,
                    RelationshipManager.this.lwidth,
                    RelationshipManager.this.layout.getContentHeight(),
                    RelationshipManager.this.layout.getHeaderHeight(),
                    15
            );
            this.filterEntries("");
        }

        private void filterEntries(final String filter) {
            List<RelationshipManager.PlayerList.Entry> list = RelationshipManager.this.players
                    .stream()
                    .map(Entry::new)
                    .filter(entry -> filter.isEmpty() || entry.player.toLowerCase().contains(filter.toLowerCase()))
                    .toList();
            this.replaceEntries(list);
            this.refreshScrollAmount();
        }

        public void setSelected(final RelationshipManager.PlayerList.@Nullable Entry selected) {
            super.setSelected(selected);
            if (selected != null) {
                RelationshipManager.this.selectedPlayer = selected.player;
                if (Config.relations.containsKey(selected.player) && RelationshipManager.this.tlist.children().size() == Config.tags.size()) RelationshipManager.this.tlist.setSelected(RelationshipManager.this.tlist.children().stream().filter(e -> Objects.equals(e.tag, Config.relations.get(selected.player.toLowerCase()))).iterator().next()); // FIXME: kill this
            }
        }

        private class Entry extends ObjectSelectionList.Entry<RelationshipManager.PlayerList.Entry> {
            private final String player;

            public Entry(final String player) {
                this.player = player;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", "");
            }

            @Override
            public void renderContent(final GuiGraphics graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
                int col = 0xFFFFFFFF;
                if (Config.relations.containsKey(this.player.toLowerCase())) col = Config.tags.get(Config.relations.get(this.player.toLowerCase()));
                graphics.drawString(RelationshipManager.this.font, Component.literal(this.player).withColor(col), this.getContentX() + 5, this.getContentY() + 2, -1);
            }

            @Override
            public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
                PlayerList.this.setSelected(this);
                return super.mouseClicked(event, doubleClick);
            }
        }
    }
    // FIXME: merge?
    private class TagList extends ObjectSelectionList<RelationshipManager.TagList.Entry> {
        private TagList() {
            super(
                    RelationshipManager.this.minecraft,
                    RelationshipManager.this.lwidth,
                    RelationshipManager.this.layout.getContentHeight(),
                    RelationshipManager.this.layout.getHeaderHeight(),
                    15
            );
            this.filterEntries("");
        }

        private void filterEntries(final String filter) {
            List<RelationshipManager.TagList.Entry> list = new java.util.ArrayList<>(Config.tags.keySet()
                    .stream()
                    .map(Entry::new)
                    .filter(entry -> filter.isEmpty() || entry.tag.toLowerCase().contains(filter.toLowerCase()))
                    .toList());
            list.add(new Entry("none"));
            this.replaceEntries(list);
            this.refreshScrollAmount();
        }

        public void setSelected(final RelationshipManager.TagList.@Nullable Entry selected) {
            super.setSelected(selected);
            if (selected != null) {
                RelationshipManager.this.selectedTag = selected.tag;
            }
        }

        private class Entry extends ObjectSelectionList.Entry<RelationshipManager.TagList.Entry> {
            private final String tag;

            public Entry(final String tag) {
                this.tag = tag;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", "");
            }

            @Override
            public void renderContent(final GuiGraphics graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
                Integer col = Config.tags.get(this.tag);
                if (col == null) col = 0xFFFFFF;
                graphics.drawString(RelationshipManager.this.font, Component.literal(this.tag).withColor(col), this.getContentX() + 5, this.getContentY() + 2, -1);
            }

            @Override
            public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
                TagList.this.setSelected(this);
                return super.mouseClicked(event, doubleClick);
            }
        }
    }

    private void updatePos() {
        this.plist.updateSizeAndPosition(this.lwidth, this.layout.getContentHeight(), 0, this.layout.getHeaderHeight());
        this.tlist.updateSizeAndPosition(this.lwidth, this.layout.getContentHeight(), this.lwidth*2, this.layout.getHeaderHeight());
    }
    
    // FIXME: make this global
    public static boolean showGUI(String cmd) {
        Screen s = Minecraft.getInstance().screen;
        if (s != null && s.getClass() != ChatScreen.class) return true;
        Minecraft.getInstance().setScreen(new RelationshipManager());
        return true;
    }
}
