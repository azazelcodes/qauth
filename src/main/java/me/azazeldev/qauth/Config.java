package me.azazeldev.qauth;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config extends MidnightConfig {
    public static final String UI = "ui";
    @Entry(category = UI) public static boolean renderStash = true;
    @Entry(category = UI) public static boolean renderQuest = true;
    @Entry(category = UI) public static boolean flipHUD = false;
    @Entry(category = UI) public static boolean renderEvents = true;
    @Entry(category = UI) public static List<Item> valuables = Lists.newArrayList(Items.TWISTING_VINES, Items.PRISMARINE_SHARD, Items.BLAZE_ROD, Items.BREEZE_ROD);

    @Entry(category = UI) public static boolean markBarrels = true;
    // TODO: add color config for markers


    @Entry(category = UI) public static float slotSize = 1;
    @Entry(category = UI) public static boolean dev = false;


    public static final String UTIL = "util";
    @Entry(category = UTIL) public static boolean keepExtractCmd = false;
    @Entry(category = UTIL) public static boolean alwaysAllDrop = true;
    @Entry(category = UTIL) public static boolean noDropHand = true;

    @Entry(category = UTIL) public static Map<Integer, ItemStack> stash = new HashMap<>(); // FIXME: if possible, move to list, ContainerMixins Config.write is slow though, so the render thread causes an ioob
    @Entry(category = UTIL) public static Map<String, List<JsonObject>> quests = new HashMap<>(); // <NPC, Quest>


    public static <T> boolean toggleItem(List<T> list, T target) {
        boolean c = list.contains(target);
        if (c) list.remove(target); else list.add(target);
        Config.write(Main.MOD_ID);
        return !c;
    }
}
