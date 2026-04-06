package me.azazeldev.qauth.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MainClient implements ClientModInitializer {
    private static final Identifier HOTBAR_TEXTURE = Identifier.fromNamespaceAndPath("qauth", "textures/gui/hotbar_texture.png");

    public static KeyMapping markValuable;
    public static String[] nostash = { "[ ]", "[Upgrade]" };

    @Override
    public void onInitializeClient() {
        KeyMapping.Category MAIN_CATEGORY = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(Main.MOD_ID, "qauth")
        );
        markValuable = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.qauth.mark_valuable",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        MAIN_CATEGORY
                )
        );

        CommandHandler.popCmds();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            CommandHandler.registerBrigadier(dispatcher);
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath(Main.MOD_ID, "stash_preview"), MainClient::StashPreview); // FIXME: draw above all screens, even containers - how? idk
    }

    private static void StashPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (!Config.renderStash) return;
        List<Map.Entry<Integer, ItemStack>> stash = Config.stash.entrySet().stream().filter(e -> !e.getValue().isEmpty()).toList();
        int slotSize = (int) (0.75f*24);
        Window window = Minecraft.getInstance().getWindow();

        graphics.pose().pushMatrix();
        graphics.pose().scale(Config.slotSize*0.75f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
                (int)(Config.alignStashRight ? window.getGuiScaledWidth()*(1/0.75f)/Config.slotSize - 9*24 - 2 : 2),2,
                    0,0,
                    9*24,(int) Math.ceil((double)stash.size()/9)*24,
                24,24
        );
        graphics.pose().popMatrix();

        int i = 0;
        for (Map.Entry<Integer, ItemStack> e : stash) { // FIXME: CENTER THESE BETTER!! fwiquin hud changes. 1.21 was so much better </3
            int ix = (int)(Config.alignStashRight ? window.getGuiScaledWidth()/Config.slotSize - 9*slotSize - 2 + (i % 9)*slotSize : 2 + (i % 9)*slotSize);
            int iy = 2 + (int) Math.floor((double)i/9)*slotSize;
            graphics.pose().pushMatrix();
            graphics.pose().scale(Config.slotSize);
            graphics.renderItem(
                    e.getValue(),
                    ix, iy
            );
            i++;
            if (e.getValue().getCount() <= 1) { graphics.pose().popMatrix(); continue; }
            Font font = Minecraft.getInstance().font;
            graphics.textRenderer().accept(ix+slotSize-font.width(String.valueOf(e.getValue().getCount()))-1, iy+slotSize-font.lineHeight-1, Component.literal(String.valueOf(e.getValue().getCount())).withColor(0xFFA9A9A9));
            graphics.pose().popMatrix();
        }
    }


    public static void sendClient(Component msg) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("qa>>").withStyle(ChatFormatting.ITALIC).withColor(0xC889CFF0).append(Component.literal(" ").withStyle(ChatFormatting.RESET).append(msg))); // TODO: move to minimessage
    }
}
