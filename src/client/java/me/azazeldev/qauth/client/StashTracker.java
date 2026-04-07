package me.azazeldev.qauth.client;

import com.mojang.blaze3d.platform.Window;
import me.azazeldev.qauth.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class StashTracker {
    private static final Identifier HOTBAR_TEXTURE = Identifier.fromNamespaceAndPath("qauth", "textures/gui/hotbar_texture.png");
    public static String[] nostash = {" ", "Upgrade"};
    static void StashPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (!Config.renderStash) return;
        List<Map.Entry<Integer, ItemStack>> stash = Config.stash.entrySet().stream().filter(e -> !e.getValue().isEmpty()).toList();
        int slotSize = (int) (0.75f * 24);
        Window window = Minecraft.getInstance().getWindow();

        graphics.pose().pushMatrix();
        graphics.pose().scale(Config.slotSize * 0.75f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
                (int) (Config.alignStashRight ? window.getGuiScaledWidth() * (1 / 0.75f) / Config.slotSize - 9 * 24 - 2 : 2), 2,
                0, 0,
                9 * 24, (int) Math.ceil((double) stash.size() / 9) * 24,
                24, 24
        );
        graphics.pose().popMatrix();

        int i = 0;
        for (Map.Entry<Integer, ItemStack> e : stash) { // FIXME: CENTER THESE BETTER!! fwiquin hud changes. 1.21 was so much better </3
            int ix = (int) (Config.alignStashRight ? window.getGuiScaledWidth() / Config.slotSize - 9 * slotSize - 2 + (i % 9) * slotSize : 2 + (i % 9) * slotSize);
            int iy = 2 + (int) Math.floor((double) i / 9) * slotSize;
            graphics.pose().pushMatrix();
            graphics.pose().scale(Config.slotSize);
            graphics.renderItem(
                    e.getValue(),
                    ix, iy
            );
            i++;
            if (e.getValue().getCount() <= 1) {
                graphics.pose().popMatrix();
                continue;
            }
            Font font = Minecraft.getInstance().font;
            graphics.textRenderer().accept(ix + slotSize - font.width(String.valueOf(e.getValue().getCount())) - 1, iy + slotSize - font.lineHeight - 1, Component.literal(String.valueOf(e.getValue().getCount())).withColor(0xFFA9A9A9));
            graphics.pose().popMatrix();
        }
    }
}
