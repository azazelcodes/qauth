package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.client.Config;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class CPlayerMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void onDrop(boolean bl, CallbackInfoReturnable<Boolean> ci) { if (Config.noDropHand) ci.setReturnValue(false); }
}
