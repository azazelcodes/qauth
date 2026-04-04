package me.azazeldev.qauth.client;

import com.google.common.base.CharMatcher;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import eu.midnightdust.lib.config.MidnightConfig;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.Map;

public class MainClient implements ClientModInitializer {
    private static final Identifier HOTBAR_TEXTURE = Identifier.fromNamespaceAndPath("qauth", "textures/gui/hotbar_texture.png");

    public static KeyMapping markValuable;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category MAIN_CATEGORY = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(Main.MOD_ID, "qauth")
        );
        markValuable = KeyMappingHelper.registerKeyMapping(
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

    private static void StashPreview(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!Config.renderStash) return;
        int slotSize = (int) (0.75f*24);
        Window window = Minecraft.getInstance().getWindow();

        graphics.pose().pushMatrix();
        graphics.pose().scale(Config.slotSize*0.75f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
                (int)(Config.alignStashRight ? window.getGuiScaledWidth()*(1/0.75f)/Config.slotSize - 9*24 - 2 : 2),2,
                    0,0,
                    9*24,(int) Math.ceil((double)Config.stash.size()/9)*24,
                24,24
        );
        graphics.pose().popMatrix();

        int i = 0;
        for (ItemStack it : Config.stash) { // FIXME: CENTER THESE BETTER!! fwiquin hud changes. 1.21 was so much better </3
            int ix = (int)(Config.alignStashRight ? window.getGuiScaledWidth()/Config.slotSize - 9*slotSize - 2 + (i % 9)*slotSize : 2 + (i % 9)*slotSize);
            int iy = 2 + (int) Math.floor((double)i/9)*slotSize;
            graphics.pose().pushMatrix();
            graphics.pose().scale(Config.slotSize);
            graphics.item(
                    it,
                    ix, iy
            );
            i++;
            if (it.count() <= 1) { graphics.pose().popMatrix(); continue; }
            Font font = Minecraft.getInstance().font;
            graphics.text(font, String.valueOf(it.count()), ix+slotSize-font.width(String.valueOf(it.count()))-1, iy+slotSize-font.lineHeight-1, 0xFFA9A9A9);
            graphics.pose().popMatrix();
        }
    }


    public static void sendClient(Component msg) {
        Minecraft.getInstance().gui.getChat().addClientSystemMessage(Component.literal("qa>>").withStyle(ChatFormatting.ITALIC).withColor(0xC889CFF0).append(Component.literal(" ").withStyle(ChatFormatting.RESET).append(msg))); // TODO: move to minimessage
    }
}
