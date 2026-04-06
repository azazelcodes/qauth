package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class EntityMixin { // TODO: rename
    @Inject(method = "belowNameDisplay()Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    public void subname(CallbackInfoReturnable<@Nullable Component> ci) {
        Entity e = (Entity) (Object) this;
        if (!(e instanceof Player)) return;

        Component n = Config.getRel(e.getName().getString().toLowerCase());

        if (n.getString().isEmpty()) return;
        if (ci.getReturnValue() != null) n = n.copy().append(ci.getReturnValue());
        ci.setReturnValue(n);
    }
}
