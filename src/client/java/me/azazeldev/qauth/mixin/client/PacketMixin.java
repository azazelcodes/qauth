package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.gui.EventTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
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
    ))
    private void onSChat(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        List<Component> l = clientboundSystemChatPacket.content().toFlatList();
        String f = l.getFirst().getString();
        // if (f.contains("active")) EventTracker.refresh(); // TODO:
        if (f.contains("cooldown:")) EventTracker.parse(l.getLast().getString(), f);
    }
}
