package me.azazeldev.qauth.client;

import me.azazeldev.qauth.client.gui.StatTracker;
import net.minecraft.world.entity.EntityType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.contains;

public class StateManager {
    private static AuthState state = AuthState.OFFLINE;
    public static Long stateChanged;

    public static AuthState getState() {
        return state;
    }

    public static void setState(AuthState state) {
        if (state != StateManager.state && (state == AuthState.LOBBY || state == AuthState.RAID)) stateChanged = Instant.now().toEpochMilli();
        StateManager.state = state;
        if (state == AuthState.LOBBY) StatTracker.kills.clear();
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
}
