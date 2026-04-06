package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class EntityMixin {
    @Inject(method = "belowNameDisplay()Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    public void subname(CallbackInfoReturnable<@Nullable Component> ci) {
        Entity e = (Entity) (Object) this;
        if (!(e instanceof Player)) return;
        Component n = Component.empty();

        if (Config.tm8s.contains(e.getName().getString().toLowerCase())) n = Component.literal("[team]").withStyle(ChatFormatting.BOLD).withColor(0xFF00FF00);
        if (Config.wars.contains(e.getName().getString().toLowerCase())) n = Component.literal("[enemy]").withStyle(ChatFormatting.BOLD).withColor(0xFFFF0000);

        if (n.getString().isEmpty()) return;
        if (ci.getReturnValue() != null) n = n.copy().append(ci.getReturnValue());
        ci.setReturnValue(n);
    }
}
