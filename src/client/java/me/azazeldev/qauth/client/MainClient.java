package me.azazeldev.qauth.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainClient implements ClientModInitializer {
    private static Screen lastScreen = null;
    public static ArrayList<Pair<Integer, Integer>> full = new ArrayList<>();
    public static ArrayList<Pair<Integer, Integer>> same = new ArrayList<>();
    public static ArrayList<Pair<Integer, Integer>> unowned = new ArrayList<>(); // because new doesn't work >:(

    public static int invCutoff = 0;


    private static KeyBinding qSwitchF;
    private static KeyBinding qSwitchB;
    private static KeyBinding qgSwitchF;
    private static KeyBinding qgSwitchB;
    public static HashMap<Text, HashMap<Text, LoreComponent>> quests = new HashMap<>(); // NPC_NAME{QUEST_NAME, QUEST_DESCRIPTION}
    public static int qIndex = 0;
    public static int qgIndex = 0;


    private static final Identifier HOTBAR_TEXTURE = Identifier.of("qauth", "textures/gui/hotbar_texture.png");
    public static KeyBinding dropstack;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            popSlots();


            Text selectedQG = getSelectedQG();
            while (qSwitchF.wasPressed()) {
                if (qIndex+1 < quests.get(selectedQG).size()) {
                    qIndex += 1;
                } else {
                    qIndex = 0;
                }
            }
            while (qSwitchB.wasPressed()) {
                if (qIndex-1 < 0) {
                    qIndex = quests.get(selectedQG).size()-1;
                } else {
                    qIndex -= 1;
                }
            }

            while (qgSwitchF.wasPressed()) {
                qIndex = 0;
                if (qgIndex+1 < quests.size()) {
                    qgIndex += 1;
                } else {
                    qgIndex = 0;
                }
            }
            while (qgSwitchB.wasPressed()) {
                qIndex = 0;
                if (qgIndex-1 < 0) {
                    qgIndex = quests.size()-1;
                } else {
                    qgIndex -= 1;
                }
            }
        });


        qSwitchF = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.qswitchf",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_3,
                "category.qauth.quests"
        ));
        qSwitchB = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.qswitchb",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_1,
                "category.qauth.quests"
        ));

        qgSwitchF = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.qgswitchf",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_6,
                "category.qauth.quests"
        ));
        qgSwitchB = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.qgswitchb",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_4,
                "category.qauth.quests"
        ));

        HudRenderCallback.EVENT.register((context, delta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            // Get screen width and height
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            /*context.drawTexture(
                    RenderLayer::getGuiTexturedOverlay,
                    HOTBAR_TEXTURE,
                    screenWidth - 22 - 7, 7,
                    0f, 0f,
                    22, 22,
                    22, 22
            );*/

            context.drawText(
                    client.textRenderer,
                    getSelectedQG(),
                    8, 32,
                    0xFFFFFFFF, false
            );
            context.drawText(
                    client.textRenderer,
                    getSelectedQ().getKey(),
                    8, 52,
                    0xFFFFFFFF, false
            );
            int i = 0;
            for (Text t : getSelectedQ().getValue().lines()) {
                context.drawText(
                        client.textRenderer,
                        t,
                        8, 64 + i*8,
                        0xFFFFFFFF, false
                );
                i += 1;
            }
        });


        dropstack = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.dropstack",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.qauth.shortcuts"
        ));
    }

    public static void popSlots() {
        full = new ArrayList<>();
        same = new ArrayList<>();
        unowned = new ArrayList<>(); // because new doesn't work >:(


        HashMap<Item, ArrayList<Pair<Integer, Integer>>> cont = new HashMap<>();
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;

        if (currentScreen instanceof HandledScreen<?> handledScreen && currentScreen != lastScreen) {
            //lastScreen = currentScreen;
            int totalSlots = handledScreen.getScreenHandler().slots.size();
            invCutoff = totalSlots - 36; // Usually last 36 slots are player inventory

            // CONTAINER INV
            for (int i = 0; i < invCutoff; i++) {
                Slot slot = handledScreen.getScreenHandler().slots.get(i);
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    ArrayList<Pair<Integer, Integer>> list = cont.getOrDefault(stack.getItem(), new ArrayList<>());
                    list.add(new Pair<>(slot.x, slot.y));
                    cont.put(stack.getItem(), list);
                }
            }

            // PLAYER INV
            for (int i = invCutoff; i < totalSlots; i++) {
                Slot slot = handledScreen.getScreenHandler().slots.get(i);
                ItemStack stack = slot.getStack();
                if (stack.getCount() == stack.getMaxCount()) {
                    full.add(new Pair<>(slot.x, slot.y));
                    //cont.remove(stack.getItem());
                    continue;
                }
                if (cont.containsKey(stack.getItem())) {
                    same.add(new Pair<>(slot.x, slot.y));
                    same.addAll(cont.get(stack.getItem()));
                    cont.remove(stack.getItem());
                }
            }

            for (Map.Entry<Item, ArrayList<Pair<Integer, Integer>>> e : cont.entrySet()) {
                unowned.addAll(e.getValue());
            }
        }

        if (currentScreen == null) {
            lastScreen = null;
        }
    }

    public static boolean isFull(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).isEmpty()) {
                return false;
            }
        }
        return !inventory.getStack(40).isEmpty(); // If nothing is in the offhand either
    }

    public static Text getSelectedQG() {
        Object[] keys = quests.keySet().toArray();
        Text selectedQG = Text.literal("");

        if (qgIndex >= 0 && qgIndex < keys.length) {
            selectedQG = (Text) keys[qgIndex];
        }

        return selectedQG;
    }
    public static Map.Entry<Text, LoreComponent> getSelectedQ() {
        Text selectedQG = getSelectedQG();
        Map.Entry<Text, LoreComponent> selectedQ = new HashMap.SimpleEntry<>(Text.literal(""), LoreComponent.DEFAULT);

        if (Objects.equals(selectedQG, Text.literal(""))) {
            return selectedQ;
        }

        Object[] keys = quests.get(selectedQG).entrySet().toArray();

        if (qIndex >= 0 && qIndex < keys.length) {
            selectedQ = (Map.Entry<Text, LoreComponent>) keys[qIndex];
        }

        return selectedQ;
    }
}
