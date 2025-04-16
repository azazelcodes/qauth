package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    protected T handler;

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Unique
    String title = "";
    @Unique
    boolean sellConfirm = false;
    @Unique
    int hackyPageTrigger = 0;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onOpen(CallbackInfo ci) {
        title = super.getTitle().getString();
        MainClient.containers.remove(title);
        //MainClient.popSlots();
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"))
    private void onClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot != null && slot.hasStack() && !slot.getStack().isEmpty()) {
            ItemStack stack = slot.getStack();

            MainClient.popSlots();
            sellConfirm = false;

            if (stack.getItem().equals(Items.ARROW)) {
                LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
                if (lore != null && !lore.lines().isEmpty() && lore.lines().getFirst().getString().contains("Page: ")) {
                    MainClient.pageSwitched = true;
                    MainClient.page = Integer.parseInt(lore.lines().getFirst().getString().substring(6));
                }
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == MainClient.dropstack.boundKey.getCode() && focusedSlot != null && focusedSlot.hasStack()) {
            client.interactionManager.clickSlot(
                    handler.syncId,
                    focusedSlot.id,
                    1,
                    SlotActionType.THROW,
                    client.player
            );
        }

        if (
                keyCode == GLFW.GLFW_KEY_ESCAPE
                && MainClient.containsStashable
                && MainClient.npcs.contains(title)
        ) {
            if (!sellConfirm) {
                sellConfirm = true;
                cir.setReturnValue(true);
                cir.cancel();
            }
        }

        if (keyCode == MainClient.markValuable.boundKey.getCode() && focusedSlot != null && focusedSlot.hasStack() && !focusedSlot.getStack().isEmpty()) {
            Text n = focusedSlot.getStack().getComponents().get(DataComponentTypes.CUSTOM_NAME);
            if (MainClient.marked.contains(n)) MainClient.marked.remove(n);
            else MainClient.marked.add(n);
        }
    }

    @Inject(method = "drawForeground", at = @At("HEAD"))
    private void onDrawSlots(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (MainClient.pageSwitched) {
            if (hackyPageTrigger == 20) { // assume 20 tps max?
                hackyPageTrigger = 0;
                MainClient.pageSwitched = false;
            }
            else hackyPageTrigger += 1;
        }
        for (Pair<Integer, Integer> p : MainClient.same) {
            context.fill(
                    p.getKey(), p.getValue(),
                    p.getKey() + 16, p.getValue() + 16,
                    0xA800FF00
            );
        }
        /*if (!MainClient.isFull(client.player.getInventory())) { // IS USELESS D:
            for (Pair<Integer, Integer> p : MainClient.unowned) {
                context.fill(
                    p.getKey(), p.getValue(),
                    p.getKey() + 16, p.getValue() + 16,
                    0xA80000FF
                );
            }
        }*/
        for (Pair<Integer, Integer> p : MainClient.full) {
            context.fill(
                    p.getKey(), p.getValue(),
                    p.getKey() + 16, p.getValue() + 16,
                    0xA8FF0000
            );
        }

        for (Pair<Integer, Integer> p : MainClient.stashable) {
            context.fill(
                    p.getKey(), p.getValue(),
                    p.getKey() + 16, p.getValue() + 16,
                    0xA8D2BD96 // 5D3FD3
            );
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (slot != null) {
            //context.drawText(client.textRenderer, String.valueOf(slot.getIndex()), slot.x, slot.y, 0xFFFFFFFF, false);

            if (
                    slot.hasStack()
                    && !slot.getStack().isEmpty()
                    && slot.getStack().getComponents().get(DataComponentTypes.CUSTOM_NAME) != null
                    && MainClient.marked.contains(slot.getStack().getComponents().get(DataComponentTypes.CUSTOM_NAME))
            ) {
                context.fill(
                        slot.x, slot.y,
                        slot.x + 16, slot.y + 16,
                        0xA80000FF // repurposed haha
                );
            }
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void onClose(CallbackInfo ci) {
        MainClient.page = 0;

        boolean containsQuests = false;
        HashMap<Text, LoreComponent> quests = new HashMap<>();
        for (ItemStack s : handler.getStacks()) {
            Item i = s.getItem();
            if (i == Items.GREEN_STAINED_GLASS_PANE) { containsQuests = true; } // Available quest
            if (i == Items.RED_STAINED_GLASS_PANE) { containsQuests = true; } // Available quest (ALSO IN STASH)
            if (i == Items.GOLD_NUGGET && containsQuests) { // Upgrades (like stash and hideout)
                ComponentMap c = s.getComponents();
                Text name = c.get(DataComponentTypes.CUSTOM_NAME);
                if (!name.getString().equals("Gold Nugget")) {
                    quests.put(name, c.get(DataComponentTypes.LORE));
                }
            }
            if (i == Items.ORANGE_STAINED_GLASS_PANE) { // Accepted quest
                containsQuests = true;
                ComponentMap c = s.getComponents();
                quests.put(c.get(DataComponentTypes.CUSTOM_NAME), c.get(DataComponentTypes.LORE));
            }
        }
        if (containsQuests && !quests.isEmpty()) MainClient.quests.put(title, quests);



        if (title.equals("Generator")) {
            // slot 3 contains timer
        }

        if (title.contains("Station") && !title.contains("Recipes")) {
            int totalSlots = handler.slots.size();
            int invCutoff = totalSlots - 36; // Usually last 36 slots are player inventory

            for (int i = 7; i < invCutoff; i+=18) { // only column 7, every two rows
                Slot slot = handler.slots.get(i);
                ItemStack stack = slot.getStack();
                if (stack.getItem() != Items.RED_STAINED_GLASS_PANE && !stack.isEmpty()) {
                    LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
                    String timer = lore.lines().getLast().getString();

                    int hours = 0, minutes = 0, seconds = 0;

                    Pattern pattern = Pattern.compile("(\\d+)h|\\s*(\\d+)m|\\s*(\\d+)s");
                    Matcher matcher = pattern.matcher(timer);

                    while (matcher.find()) {
                        if (matcher.group(1) != null) {
                            hours = Integer.parseInt(matcher.group(1));
                        } else if (matcher.group(2) != null) {
                            minutes = Integer.parseInt(matcher.group(2));
                        } else if (matcher.group(3) != null) {
                            seconds = Integer.parseInt(matcher.group(3));
                        }
                    }

                    Duration duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
                    long currentUnixTime = Instant.now().getEpochSecond();
                    long futureUnixTime = currentUnixTime + duration.getSeconds();

                    Pair<String, Integer> identifier = Pair.of(title, i);
                    if (MainClient.crafting.containsKey(identifier)) {
                        MainClient.crafting.replace(
                                identifier,
                                Pair.of(stack, futureUnixTime)
                        );
                    } else {
                        MainClient.crafting.put(
                                identifier,
                                Pair.of(stack, futureUnixTime)
                        );
                    }
                }
            }
        }
    }
}