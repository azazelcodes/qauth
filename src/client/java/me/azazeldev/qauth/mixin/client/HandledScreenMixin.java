package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
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
import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

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
    boolean needConfirm = false;

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
                && MainClient.npcs.contains(super.getTitle().getString())
        ) {
            if (!needConfirm) {
                needConfirm = true;
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
                        p.getA(), p.getB(),
                        p.getA() + 16, p.getB() + 16,
                        0xA800FF00
                );
            }
            /*if (!MainClient.isFull(client.player.getInventory())) { // IS USELESS D:
                for (Pair<Integer, Integer> p : MainClient.unowned) {
                    context.fill(
                            p.getA(), p.getB(),
                            p.getA() + 16, p.getB() + 16,
                            0xA80000FF
                    );
                }
            }*/
            for (Pair<Integer, Integer> p : MainClient.full) {
                context.fill(
                        p.getA(), p.getB(),
                        p.getA() + 16, p.getB() + 16,
                        0xA8FF0000
                );
            }

            for (Pair<Integer, Integer> p : MainClient.stashable) {
                context.fill(
                        p.getA(), p.getB(),
                        p.getA() + 16, p.getB() + 16,
                        0xA8FE019A
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
        if (containsQuests && !quests.isEmpty()) MainClient.quests.put(super.getTitle(), quests);
    }
}