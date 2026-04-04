package me.azazeldev.qauth;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
    public static String MOD_ID = "qauth";
    @Override
    public void onInitialize() {
        MidnightConfig.init("qauth", Config.class);
    }
}
