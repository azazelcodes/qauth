package me.azazeldev.qauth.client;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.fromNamespaceAndPath("qauth", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("qauth.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    public static Screen buildUI(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("qauth config"))

                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("UI"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("qauth.config.ui.stash"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.generic.enable"))
                                        .binding(true, () -> renderStash, newVal -> renderStash = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("qauth.config.ui.stash.size"))
                                        .binding(1f, () -> slotSize, newVal -> slotSize = newVal)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.05f, 3f).step(0.05f))
                                        .build()
                                )
                                .build()
                        )
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("qauth.config.ui.extra"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.ui.quest"))
                                        .binding(true, () -> renderQuest, newVal -> renderQuest = newVal)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.ui.event"))
                                        .binding(true, () -> renderEvents, newVal -> renderEvents = newVal)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal(""))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.ui.flip"))
                                        .binding(false, () -> flipHUD, newVal -> flipHUD = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("qauth.config.util"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal(""))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.util.keepextract"))
                                        .binding(true, () -> keepExtractCmd, newVal -> keepExtractCmd = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.util.alldrop"))
                                        .binding(true, () -> alwaysAllDrop, newVal -> alwaysAllDrop = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.util.handdrop"))
                                        .binding(true, () -> noDropHand, newVal -> noDropHand = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("qauth.config.util.hideni"))
                                        .binding(false, () -> hideNearInteract, newVal -> hideNearInteract = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )

                                .build()
                        )
                        .build()
                )

                .build()
                .generateScreen(parent);
    }

    // UI
    @SerialEntry
    public static boolean renderStash = true;
    @SerialEntry
    public static float slotSize = 1;

    @SerialEntry
    public static boolean renderQuest = true;

    @SerialEntry
    public static boolean renderEvents = true;

    @SerialEntry
    public static boolean flipHUD = false;

    @SerialEntry
    public static boolean markBarrels = true;


    // Util
    @SerialEntry
    public static boolean keepExtractCmd = false;
    @SerialEntry
    public static boolean alwaysAllDrop = true;
    @SerialEntry
    public static boolean noDropHand = true;
    @SerialEntry
    public static boolean hideNearInteract = false;



    // this is just to save data, not for "configuration"
    @SerialEntry
    public static List<Item> valuables = Lists.newArrayList(Items.TWISTING_VINES, Items.PRISMARINE_SHARD, Items.BLAZE_ROD, Items.BREEZE_ROD);
    @SerialEntry // FIXME: not serializable because of Option<>
    public static List<ItemStack> stash = Lists.newArrayList(); // FIXME: if possible, move to list
    @SerialEntry
    public static Map<String, List<JsonObject>> quests = new HashMap<>(); // <NPC, Quest>

    public static <T> boolean toggleItem(List<T> list, T target) {
        boolean c = list.contains(target);
        if (c) list.remove(target); else list.add(target);
        return !c;
    }
}