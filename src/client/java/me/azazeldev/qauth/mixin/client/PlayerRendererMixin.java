package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class PlayerRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {
    /* // FIXME: this might be a little cheaty, gotta ask goat (update, karthy said no to the whole team system)
    @Inject(method = "shouldShowName", at = @At("RETURN"), cancellable = true)
    private void overrideName(final AvatarlikeEntity entity, final double distanceToCameraSq, CallbackInfoReturnable<Boolean> ci) { ci.setReturnValue(true); }
    */
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void overrideBelowName(final AvatarlikeEntity entity, final AvatarRenderState state, final float partialTicks, CallbackInfo ci) { if (state.scoreText == null) state.scoreText = entity.belowNameDisplay(); }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = @At("RETURN"), cancellable = true)
    private void overrideName(final AvatarlikeEntity entity, final double distanceToCameraSq, CallbackInfoReturnable<Boolean> ci) { boolean bl = !entity.isInvisibleTo(Minecraft.getInstance().player) && Minecraft.renderNames() && !entity.isVehicle(); if (bl) ci.setReturnValue(true); } // this should be better than whats above, its from LivingEntityRenderer

    @Inject(method = "isPlayerUpsideDown(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true)
    private static void overrideFlip(final Player player, CallbackInfoReturnable<Boolean> ci) { String n = player.getGameProfile().name().toLowerCase(); if (Config.relations.containsKey(n) && Config.flip.contains(Config.relations.get(n))) ci.setReturnValue(true); } // maybe move to player.getName().toString() ?
}
