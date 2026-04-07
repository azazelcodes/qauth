package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.client.CommandHandler;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatMixin {
    @Inject(method = "normalizeChatMessage(Ljava/lang/String;)Ljava/lang/String;", at = @At(value = "RETURN"), cancellable = true)
    private void onNorm(String msg, CallbackInfoReturnable<String> ci) {
        msg = ci.getReturnValue();
        if (CommandHandler.execute(msg)) {
            Minecraft.getInstance().gui.getChat().addRecentChat(msg);
            ci.setReturnValue("");
        }
    }

    @Inject(method = "handleChatInput(Ljava/lang/String;Z)V", at = @At(value = "RETURN"))
    private void onChat(String msg, final boolean addToRecent, CallbackInfo ci) { if (Config.keepExtractCmd) Minecraft.getInstance().gui.getChat().addRecentChat("/extracts"); }

    @Inject(method = "keyPressed", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            shift = At.Shift.BEFORE
    ), cancellable = true)
    private void onKeyPress(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> ci) { if (Minecraft.getInstance().screen.getClass() != ChatScreen.class) ci.cancel(); }
}
