package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.client.StateManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GUIMixin {
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onTitle(Component component, CallbackInfo ci) {
        if (component.toFlatList().isEmpty()) return;
        StateManager.setState(switch (component.toFlatList().getFirst().getString()) {
            case "1" -> StateManager.AuthState.RAID;
            case "ᴇxᴛʀᴀᴄᴛᴇᴅ", "ᴇxᴛʀᴀᴄᴛɪᴏɴ ғᴀɪʟᴇᴅ" -> StateManager.AuthState.LOBBY;
            default -> StateManager.getState();
        });
    }
}
