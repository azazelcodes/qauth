package me.azazeldev.qauth.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerMixin<T extends AbstractContainerMenu> extends Screen {
    protected ContainerMixin(Component title) { super(title); }

    @Shadow
    protected T menu;


    @Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At("RETURN"))
    private void constructed(final T menu, final Inventory inventory, final Component title, final int imageWidth, final int imageHeight, CallbackInfo ci) {
        System.out.println("Constructor!");
        //System.out.println(menu.getType().toString());
    }
    // FIXME: menu.getType crashes on player inventory => way to check if container
    @Inject(method = "init()V", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        System.out.println("Init!");
        //System.out.println(menu.getType().toString());
    }

    @Inject(method = "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;III)V",
            shift = At.Shift.BEFORE
    ))
    public void beforeRenderItem(final GuiGraphicsExtractor graphics, final Slot slot, final int mouseX, final int mouseY, CallbackInfo ci) {
        if (slot.getItem().isEmpty()) return;
        if(slot.index >= menu.slots.size()-9*4) return;
        int col = 0x00FFFFFF;

        if (minecraft.player.getInventory().contains(slot.getItem())) col = 0xA800FF00;
        if (Config.valuables.contains(slot.getItem().getItem())) col = 0xA8FFFF00;
        // TODO: add stashables
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, col);
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"))
    public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> ci) {
        if (MainClient.markValuable.matches(event)) {
            MainClient.markValuable.consumeClick();
            ItemStack hover = getHoveredSlot().getItem();
            if (!hover.isEmpty()) Config.toggleItem(Config.valuables, hover.getItem());
            ci.setReturnValue(true);
        }
    }


    // TODO: find a way to access this without abstraction?
    @Accessor("hoveredSlot")
    abstract Slot getHoveredSlot();
}
