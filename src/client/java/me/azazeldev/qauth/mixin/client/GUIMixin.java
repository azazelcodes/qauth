package me.azazeldev.qauth.mixin.client;

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
        // TODO: raid/death/extract state management
    }
}
