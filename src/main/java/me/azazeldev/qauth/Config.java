package me.azazeldev.qauth;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config extends MidnightConfig { // TODO: move teammates to custom config looping over entries and displaying all of them, if possible do this in the midnightlib window
    public static final String TEAM = "team";
    @Comment(category = TEAM) public static Comment teamtut;
    @Entry(category = TEAM) public static Map<String, Integer> tags = new HashMap<>(); // <Tag, Color> // FIXME: ugly, move to better structure
    @Entry(category = TEAM) public static Map<String, String> relations = new HashMap<>(); // <Username, Tag> // how does this render? // TODO: move to custom renderer
    @Entry(category = TEAM) public static List<String> noattack = Lists.newArrayList("team");
    @Entry(category = TEAM) public static List<String> flip = Lists.newArrayList("team");


    public static final String UI = "ui";
    @Entry(category = UI) public static boolean renderStash = true;
    @Entry(category = UI) public static boolean alignStashRight = false;
    @Entry(category = UI) public static Map<Integer, ItemStack> stash = new HashMap<>(); // FIXME: if possible, move to list, ContainerMixins Config.write is slow though, so the render thread causes an ioob
    @Entry(category = UI) public static List<Item> valuables = Lists.newArrayList(Items.TWISTING_VINES, Items.PRISMARINE_SHARD, Items.BLAZE_ROD, Items.BREEZE_ROD);

    @Entry(category = UI) public static boolean markBarrels = true;
    // TODO: add color config for markers


    @Entry(category = UI) public static float slotSize = 1;
    @Entry(category = UI) public static boolean dev = false;


    public static final String UTIL = "util";
    @Entry(category = UTIL) public static boolean keepExtractCmd = false;


    public static <T> boolean toggleItem(List<T> list, T target) {
        boolean c = list.contains(target);
        if (c) list.remove(target); else list.add(target);
        Config.write(Main.MOD_ID);
        return !c;
    }

    public static Component getRel(String name) {
        Component n = Component.empty();
        String relation = Config.relations.get(name);
        if (relation != null) {
            n = Component.literal("["+relation+"]")
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(Config.tags.get(relation));
        }
        return n;
    }
}
