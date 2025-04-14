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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.apache.commons.lang3.tuple.Pair;

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

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onOpen(CallbackInfo ci) {
        MainClient.popSlots();
        title = super.getTitle().getString();
        MainClient.containers.remove(title);
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void onClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        MainClient.popSlots();
        sellConfirm = false;
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
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (slot != null) {
            //context.drawText(client.textRenderer, String.valueOf(slot.getIndex()), slot.x, slot.y, 0xFFFFFFFF, false);

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
                        0xA85D3FD3
                );
            }
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void onClose(CallbackInfo ci) {
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