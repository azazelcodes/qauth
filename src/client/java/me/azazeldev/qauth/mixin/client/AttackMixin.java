package me.azazeldev.qauth.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.azazeldev.qauth.client.Config;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import me.azazeldev.qauth.client.gui.StatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class AttackMixin { // TODO: possibly account for fire aspect or other applied effects, probably better to move to getStats on enter raid
    @Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;)V"
    ))
    private void onHit(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity livingTarget)) return;
        if (livingTarget.isDeadOrDying()) StatTracker.incrementKills(livingTarget.getType());
    }

    @Inject(method = "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V",
            shift = At.Shift.BEFORE
    ))
    private void onSweep(Entity entity, float baseDamage, DamageSource damageSource, float attackStrengthScale, CallbackInfo ci,
                         @Local LivingEntity nearby) {
        if (nearby.isDeadOrDying()) StatTracker.incrementKills(nearby.getType());
    }
}
