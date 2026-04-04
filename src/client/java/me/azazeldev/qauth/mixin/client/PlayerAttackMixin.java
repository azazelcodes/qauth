package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class PlayerAttackMixin { // TODO: move this to attackmixin in attack
    @Inject(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    public void attack(final Player player, final Entity entity, CallbackInfo ci) {
        if(entity.getType() != EntityType.PLAYER) return;
        if(Config.tm8s.contains(entity.getName().getString().toLowerCase())) ci.cancel();
    }
}
