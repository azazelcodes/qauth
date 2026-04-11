package me.azazeldev.qauth.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.yurisuika.raised.api.RaisedApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;

public class Compatibility implements ModMenuApi {
    public static void register() {
        RaisedApi.register("qauth:timer");
        RaisedApi.register("qauth:stash");
        RaisedApi.register("qauth:quests");
        RaisedApi.register("qauth:events");
    }

    public static void translate(GuiGraphics graphics, String l) {
        if (!FabricLoader.getInstance().isModLoaded("raised")) return;
        graphics.pose().translate(RaisedApi.getX("qauth:"+l), RaisedApi.getY("qauth:"+l));
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() { return Config::buildUI; }
}
