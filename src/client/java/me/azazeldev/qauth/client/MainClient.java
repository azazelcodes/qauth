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
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.*;

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
    public static HashMap<String, HashMap<Text, LoreComponent>> quests = new HashMap<>(); // NPC_NAME{QUEST_NAME, QUEST_DESCRIPTION}
    public static int qIndex = 0;
    public static int qgIndex = 0;
    private static KeyBinding qrm;


    private static final Identifier HOTBAR_TEXTURE = Identifier.of("qauth", "textures/gui/hotbar_texture.png");
    public static KeyBinding dropstack;


    public static HashMap<String, HashMap<Integer, ItemStack>> containers = new HashMap<>();
    public static boolean renderContainers = true;



    public static HashMap<Integer, ItemStack> stash = new HashMap<>(); // Index[Item,[x,y]]
    public static ArrayList<Pair<Integer, Integer>> stashable = new ArrayList<>();
    public static boolean containsStashable = false;

    public static ArrayList<String> npcs = new ArrayList<>(List.of("Ragman", "Therapist", "Mechanic"));
    public static HashMap<Pair<String, Integer>, Pair<ItemStack, Long>> crafting = new HashMap<>(); // [Name,Index][Item,EndTime]


    private static KeyBinding toggleCont;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            popSlots();


            Text selectedQG = getSelectedQG();
            while (qSwitchF.wasPressed() && !quests.isEmpty()) {
                if (qIndex+1 < quests.get(selectedQG.getString()).size()) {
                    qIndex += 1;
                } else {
                    qIndex = 0;
                }
            }
            while (qSwitchB.wasPressed() && !quests.isEmpty()) {
                if (qIndex-1 < 0) {
                    qIndex = quests.get(selectedQG.getString()).size()-1;
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

            while (qrm.wasPressed() && !quests.isEmpty()) {
                quests.get(selectedQG.getString()).remove(getSelectedQ().getKey(), getSelectedQ().getValue());
                if (quests.get(selectedQG.getString()).isEmpty()) quests.remove(selectedQG.getString());
                if (qgIndex > quests.size()-1) qgIndex = quests.size()-1;
            }

            while (toggleCont.wasPressed()) {
                renderContainers = !renderContainers;
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

        qrm = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.qrm",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_5,
                "category.qauth.quests"
        ));

        HudRenderCallback.EVENT.register((context, delta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

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





            if (!stash.isEmpty() && renderContainers) {
                int k = 0;
                context.drawTexture(
                        RenderLayer::getGuiTexturedOverlay,
                        HOTBAR_TEXTURE,
                        screenWidth - 16 - 9*22 - 3, 22 - 3,
                        0f, 0f,
                        9*22, 5*22,
                        22, 22
                );

                for (Map.Entry<Integer, ItemStack> e : stash.entrySet()) {
                    if (e.getKey() % 9 == 0) k += 1;
                    int ix = screenWidth - 16 - (k * 9 - e.getKey()) * 22;
                    int iy = k * 22;
                    context.drawItem(e.getValue(), ix, iy);

                    int c = e.getValue().getCount();
                    if (c > 1) {
                        context.getMatrices().push();
                        context.getMatrices().translate(0, 0, 200); // DRAW TEXT OVER ITEM

                        context.drawText(
                                client.textRenderer,
                                Text.literal(String.valueOf(c)),
                                ix, iy,
                                0xA8FFFFFF, true
                        );

                        context.getMatrices().pop();
                    }
                }
            }


            int k = 0;
            int r = -1;
            for (Pair<ItemStack, Long> e : crafting.values()) {
                if (k % 3 == 0) r += 1;
                int ix = (screenWidth - (k+1)*48) + r*3*48; // basically screen - amount of items but + every third I add the same as I subtract so its the same??
                int iy = 7 + ((!stash.isEmpty() && renderContainers) ? 128 : 0) + r*(22+16);
                context.drawItem(
                        e.getKey(),
                        ix, iy
                );

                long currentUnixTime = Instant.now().getEpochSecond();
                int totalSeconds = (int) (e.getValue() - currentUnixTime);
                int hours = totalSeconds / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 200); // DRAW TEXT OVER ITEM
                if (totalSeconds > 1) {
                    context.drawText(
                            client.textRenderer,
                            Text.literal(String.format("%d:%02d:%02d", hours, minutes, seconds)),
                            ix, iy + 22,
                            0xA8FFFFFF, true
                    );
                } else {
                    context.drawText(
                            client.textRenderer,
                            Text.literal("DONE!"),
                            ix, iy + 22,
                            0xFFFFFFFF, true
                    );
                }
                context.getMatrices().pop();

                k += 1;
            }
        });


        dropstack = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.dropstack",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.qauth.shortcuts"
        ));

        toggleCont = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qauth.togglecont",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                "category.qauth.shortcuts"
        ));
    }

    public static void popSlots() {
        full = new ArrayList<>();
        same = new ArrayList<>();
        unowned = new ArrayList<>(); // because new doesn't work >:(
        stashable = new ArrayList<>();
        containsStashable = false;


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
                    list.add(Pair.of(slot.x, slot.y));
                    cont.put(stack.getItem(), list);
                }

                // STASH
                if (
                        handledScreen.getTitle().getString().equals("Stash")
                        && stack.getItem() != Items.RED_STAINED_GLASS_PANE
                        && !stack.getName().getString().contains("Page")
                        && !stack.getName().getString().contains("Upgrade")
                ) {
                    stash.put(i, stack);
                } else if (
                        !handledScreen.getTitle().getString().equals("Stash")
                ) {
                    for (ItemStack is : stash.values()) {
                        if (is.getItem().equals(stack.getItem()) && !is.isEmpty()) {
                            if (!npcs.contains(handledScreen.getTitle().getString()) || i < handledScreen.getScreenHandler().slots.size() - (18 + 36)) { // ignore NPCs shops
                                containsStashable = true;
                                stashable.add(Pair.of(slot.x, slot.y));
                            }
                        }
                    }
                }
            }

            // PLAYER INV
            for (int i = invCutoff; i < totalSlots; i++) {
                Slot slot = handledScreen.getScreenHandler().slots.get(i);
                ItemStack stack = slot.getStack();
                if (stack.getCount() == stack.getMaxCount()) {
                    full.add(Pair.of(slot.x, slot.y));
                    //cont.remove(stack.getItem());
                    continue;
                }
                if (cont.containsKey(stack.getItem())) {
                    same.add(Pair.of(slot.x, slot.y));
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
            selectedQG = Text.literal((String) keys[qgIndex]);
        }

        return selectedQG;
    }
    public static Map.Entry<Text, LoreComponent> getSelectedQ() {
        Text selectedQG = getSelectedQG();
        Map.Entry<Text, LoreComponent> selectedQ = new HashMap.SimpleEntry<>(Text.literal(""), LoreComponent.DEFAULT);

        if (Objects.equals(selectedQG, Text.literal(""))) {
            return selectedQ;
        }

        Object[] keys = quests.get(selectedQG.getString()).entrySet().toArray();

        if (qIndex >= 0 && qIndex < keys.length) {
            selectedQ = (Map.Entry<Text, LoreComponent>) keys[qIndex];
        }

        return selectedQ;
    }
}
