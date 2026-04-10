package me.azazeldev.qauth.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AvatarRenderer.class)
public class PlayerRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {
    /*@ModifyArgs(method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    protected void scale(Args args) {
        for (int i = 0; i < 3; i++) args.set(i,0);
    }*/

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at =  @At("RETURN"), cancellable = true)
    private void showName(AvatarlikeEntity avatar, double d, CallbackInfoReturnable<Boolean> ci) { if (StateManager.getState() == StateManager.AuthState.LOBBY) ci.setReturnValue(true); }
}
