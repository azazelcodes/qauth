package me.azazeldev.qauth.client;

import me.azazeldev.qauth.client.gui.StatTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
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
        if (state != StateManager.state && !isGUI(state) && !isGUI(StateManager.state)) enterLeaveRaid();
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

    private static boolean isGUI(AuthState s) {
        return s == AuthState.QUEST
                || s == AuthState.SHOP
                || s == AuthState.STASH
                || s == AuthState.PROFILE
                || s == AuthState.MED
                || s == AuthState.SMITHING
                || s == AuthState.ARMOR_STAND
                || s == AuthState.ARMORY;
    }


    private static void enterLeaveRaid() {
        stateChanged = Instant.now().toEpochMilli();
        // Minecraft.getInstance().player.getStats(Stats.MOB_KILLS) // TODO: save stat kills to stattracker, useless because I have noticed player kills are not counted
    }
}
