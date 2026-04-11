package me.azazeldev.qauth.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import me.azazeldev.qauth.client.Config;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatTracker { // TODO: add kills
    public static Map<EntityType<?>, Integer> kills = new HashMap<>();
    public static void incrementKills(EntityType<?> e) { kills.put(e, kills.getOrDefault(e, 1)); }

    public static void TimePreview(GuiGraphics graphics, DeltaTracker delta) { // could also increment a variable by delta every call if state hasnt changed
        if (StateManager.stateChanged == null) return;
        Window window = Minecraft.getInstance().getWindow();
        String s = Instant.ofEpochMilli(Instant.now().toEpochMilli() - StateManager.stateChanged).toString().substring(11, 19);
        int x = (window.getGuiScaledWidth() - Minecraft.getInstance().font.width(s)) / 2;
        graphics.drawString(Minecraft.getInstance().font, Component.literal(s), x, 4, 0xFFFFFFFF);
    }

    public static void KillPreview(GuiGraphics graphics, DeltaTracker delta) {
        if (kills.isEmpty()) return;
        if (Minecraft.getInstance().level == null) return;

        int i = 0;
        for (Map.Entry<EntityType<?>, Integer> e : kills.entrySet()) {
            ItemStack r = switch (e.getKey().toString()) { // cba to make this not .toString
                case "entity.minecraft.player":
                    yield new ItemStack(Items.PLAYER_HEAD);

                case "entity.minecraft.zombie":
                    yield new ItemStack(Items.ZOMBIE_HEAD);
                case "entity.minecraft.vindicator":
                    yield singleHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFlZWQ5ZDhlZDE3NjllNzdlM2NmZTExZGMxNzk2NjhlZDBkYjFkZTZjZTI5ZjFjOGUwZDVmZTVlNjU3M2I2MCJ9fX0="); // skin texture https://minecraft-heads.com/custom-heads/head/28323-vindicator

                default:
                    yield singleHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5OWIwNWI5YTFkYjRkMjliNWU2NzNkNzdhZTU0YTc3ZWFiNjY4MTg1ODYwMzVjOGEyMDA1YWViODEwNjAyYSJ9fX0="); // skin texture https://minecraft-heads.com/custom-heads/head/64805-question-mark
            };
            int x = (Config.renderStash && !Config.stash.stream().filter(s -> !s.isEmpty()).toList().isEmpty() ? (int)(9*(24*Config.slotSize*0.75f)) + 4 : 4);
            graphics.renderItem(r, x,i*16);
            graphics.drawString(Minecraft.getInstance().font, Component.literal(String.valueOf(e.getValue())), x, i*16 + 4, 0xFFFFFFFF); // maybe make this gray? idrk
            i++;
        }

        /*LivingEntity living = new Zombie(EntityType.ZOMBIE, Minecraft.getInstance().level);
        int s = 15;
        int x = (int) (49 * (s/30f));
        int y = (int) (70 * (s/30f));
        renderEntityInInventoryFollowsMouse(graphics, 0, 0, x, y, s, 0.0625F, 0, 0, living);*/ // InventoryScreen method
    }

    public static ItemStack singleHead(String b64) {
        ItemStack unknown = new ItemStack(Items.PLAYER_HEAD);
        Optional<Pair<ResolvableProfile, Object>> texture = ResolvableProfile.CODEC.decode(JavaOps.INSTANCE, Map.of("properties", List.of(Map.of("name", "textures", "value", b64)))).result(); // thanks celdaemon
        if (texture.isEmpty()) return new ItemStack(Items.BLACK_CONCRETE);
        unknown.set(DataComponents.PROFILE, texture.get().getFirst());
        return unknown;
    }
}