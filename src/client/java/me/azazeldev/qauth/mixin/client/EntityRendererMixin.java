package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.client.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void renderCheck(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> ci) { // this doesnt allow players to be interacted through, so its useless for now -> TODO: find a way to make rc register through hidden players CLIENT SIDE!!
        if (!ci.getReturnValue()) return;
        if (entity.getType() != EntityType.PLAYER) return;
        if (!Config.hideNearInteract) return;
        Player p = (Player) entity;
        if (p.getGameProfile().name().equals(Minecraft.getInstance().player.getGameProfile().name())) return;
        AABB box = new AABB(p.position().x-1.8,p.position().y-5,p.position().z-1.8,
                p.position().x+1.8,p.position().y+3,p.position().z+1.8);
        if (!p.level().getEntities(
                EntityTypeTest.forClass(Display.TextDisplay.class),
                box,
                x->true
            ).isEmpty() && p.level().getEntities(
                EntityTypeTest.forClass(Display.BlockDisplay.class),
                box,
                x->true
            ).isEmpty() // do not hide near graves because that could be dangerous
        ) ci.setReturnValue(false); // this does not work that well for smithing because its x is off by 1
    }
}
