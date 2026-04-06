package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class PlayerRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {
    @Inject(method = "shouldShowName", at = @At("RETURN"), cancellable = true)
    private void overrideName(final AvatarlikeEntity entity, final double distanceToCameraSq, CallbackInfoReturnable<Boolean> ci) { ci.setReturnValue(true); } // FIXME: this might be a little cheaty, gotta ask goat

    @Inject(method = "isPlayerUpsideDown(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true)
    private static void overrideFlip(final Player player, CallbackInfoReturnable<Boolean> ci) { String n = player.getGameProfile().name().toLowerCase(); if ((Config.flipTeam && Config.tm8s.contains(n)) || (Config.flipWars && Config.wars.contains(n))) ci.setReturnValue(true); } // maybe move to player.getName().toString() ?
}
