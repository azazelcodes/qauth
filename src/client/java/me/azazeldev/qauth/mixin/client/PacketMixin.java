package me.azazeldev.qauth.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.azazeldev.qauth.client.Config;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.gui.EventTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPacketListener.class)
public class PacketMixin {
    /*   // apparently this never fires with kâthyrynes chat implementation
    @Inject(method = "handlePlayerChat", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/SignedMessageValidator;updateAndValidate(Lnet/minecraft/network/chat/PlayerChatMessage;)Lnet/minecraft/network/chat/PlayerChatMessage;",
            shift = At.Shift.AFTER
    ))
    private void onPChat(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        MainClient.sendClient(Component.literal("PChat"));
    }
    */

    @Inject(method = "handleSystemChat", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            shift = At.Shift.BEFORE
    ), cancellable = true)
    private void onSChat(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        List<Component> l = clientboundSystemChatPacket.content().toFlatList();
        String f = l.getFirst().getString();
        if (f.startsWith("Hey!")) ci.cancel();
        // if (f.contains("active")) EventTracker.refresh(); // TODO:
        if (f.contains("cooldown:")) EventTracker.parse(l.getLast().getString(), f);
    }

/*
    @Inject(method = "handleTakeItemEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;",
            shift = At.Shift.AFTER
    ))
    private void onPickup(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket, CallbackInfo ci, @Local ItemEntity itemEntity) {
        if (clientboundTakeItemEntityPacket.getPlayerId() != Minecraft.getInstance().player.getId()) return;
        if (!Config.dropPickup) return; // TODO: basically the idea is that it cancels out the pickup, which normally isnt cancellable
        ItemStack itemStack = itemEntity.getItem();
        //Minecraft.getInstance().player.getInventory()
        while (!Minecraft.getInstance().player.getInventory().contains(i -> i.is(itemStack.getItem()) && i.getCount()>=itemStack.getCount())) {
            // wait
            MainClient.sendClient("waiting for "+itemStack);
        }
        for ()
        Minecraft.getInstance().player.drop(itemStack, false);
    }
 */
}
