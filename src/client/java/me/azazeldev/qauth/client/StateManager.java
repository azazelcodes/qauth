package me.azazeldev.qauth.client;

import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class StateManager {
    private static AuthState state = AuthState.OFFLINE;

    public static AuthState getState() {
        return state;
    }

    public static void setState(AuthState state) {
        StateManager.state = state;
        if (state == AuthState.LOBBY) kills.clear();
    }

    public enum AuthState {
        OFFLINE,

        LOBBY,
        RAID,

        QUEST,
        SHOP,
        STASH,
        PROFILE,

        MED,
        SMITHING,
        ARMOR_STAND,
        ARMORY,
        BITCOIN
    }

    public static Map<EntityType<?>, Integer> kills = new HashMap<>();
    public static void incrementKills(EntityType<?> e) { kills.put(e, kills.getOrDefault(e, 1)); }
}
