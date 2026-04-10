package me.azazeldev.qauth.client.gui;

import com.mojang.blaze3d.platform.Window;
import me.azazeldev.qauth.client.Config;
import me.azazeldev.qauth.client.MainClient;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class StashTracker {
    private static final Identifier HOTBAR_TEXTURE = Identifier.fromNamespaceAndPath("qauth", "textures/gui/hotbar_texture.png");
    public static String[] nostash = {" ", "Upgrade"}; // FIXME: add next / prev page
    public static void StashPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (!Config.renderStash) return;
        int slotSize = (int) (0.75f * 24);
        Window window = Minecraft.getInstance().getWindow();

        List<ItemStack> stash = Config.stash.stream().filter(i -> !i.isEmpty()).toList();

        graphics.pose().pushMatrix();
        graphics.pose().scale(Config.slotSize * 0.75f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
                (int) (Config.flipHUD ? window.getGuiScaledWidth() * (1 / 0.75f) / Config.slotSize - 9 * 24 - 2 : 2), 2,
                0, 0,
                9 * 24, (int) Math.ceil((double) stash.size() / 9) * 24,
                24, 24
        );
        graphics.pose().popMatrix();

        int i = 0;
        for (ItemStack e : stash) { // FIXME: CENTER THESE BETTER!! fwiquin hud changes. 1.21 was so much better </3
            int ix = (int) (Config.flipHUD ? window.getGuiScaledWidth() / Config.slotSize - 9 * slotSize - 2 + (i % 9) * slotSize : 2 + (i % 9) * slotSize);
            int iy = 2 + (int) Math.floor((double) i / 9) * slotSize;
            graphics.pose().pushMatrix();
            graphics.pose().scale(Config.slotSize);
            graphics.renderItem(
                    e,
                    ix, iy
            );
            i++;
            if (e.getCount() <= 1) {
                graphics.pose().popMatrix();
                continue;
            }
            Font font = Minecraft.getInstance().font;
            graphics.textRenderer().accept(ix + slotSize - font.width(String.valueOf(e.getCount())) - 1, iy + slotSize - font.lineHeight - 1, MainClient.nativifyKyori(MiniMessage.miniMessage().deserialize("<gray><amount>", Placeholder.unparsed("amount",String.valueOf(e.getCount())))));
            graphics.pose().popMatrix();
        }
    }
}
