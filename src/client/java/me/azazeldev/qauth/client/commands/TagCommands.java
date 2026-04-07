package me.azazeldev.qauth.client.commands;

import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Objects;

public class TagCommands {
    public static boolean createTag(String cmd) {
        String[] args = cmd.split(" ");
        if (args.length != 3) { // TODO: move error handling out of this, throw InvalidArguments and handle it in cmd.apply
            MainClient.sendClient(Component.literal("Invalid Arguments! Usage: !ctag <relationship> <hex color (#ffffff for white)>"));
            return true;
        }
        if (!((args[2].startsWith("0x") && args[2].length() == 8) || (args[2].startsWith("#") && args[2].length() == 7))) {
            MainClient.sendClient(Component.literal("Please provide the color in hexadecimal format!"));
            return true;
        }

        String hex = "";
        if (args[2].startsWith("#")) {
            hex = args[2].substring(1);
        } else if (args[2].startsWith("0x")) {
            hex = args[2].substring(2);
        }

        if (hex.length() != 6) {
            MainClient.sendClient(Component.literal("Please provide the color in hexadecimal format!"));
            return true;
        }

        int col = (int)Long.parseLong(hex, 16);
        MainClient.sendClient(Component.literal(String.format("Added relationship tag %s with color #%s", args[1], hex)).withColor(col));
        Config.tags.put(args[1].toLowerCase(), 0xFF000000 | col);
        Config.write(Main.MOD_ID);
        return true;
    }
    public static boolean deleteTag(String cmd) {
        String[] args = cmd.split(" ");
        if (Config.tags.containsKey(args[1])) MainClient.sendClient(Component.literal("Removed %s from your relationship tags").withColor(0xFFFF0000));
        Config.tags.remove(args[1]);
        return true;
    }

    public static boolean tag(String cmd) {
        String[] args = cmd.split(" ");
        if (args.length != 3) { // TODO: move error handling out of this, throw InvalidArguments and handle it in cmd.apply
            MainClient.sendClient(Component.literal("Invalid Arguments! Usage: !tag <username> <relationship>"));
            return true;
        }

        String name = args[1].toLowerCase();
        String tag = args[2].toLowerCase();
        boolean isrel = Config.relations.containsKey(name) && Objects.equals(Config.relations.get(name), tag);
        Config.relations.remove(name);
        if (isrel) MainClient.sendClient(Component.literal(String.format("Removed %s from %s!", args[1], args[2])).withColor(0xFFFF0000));
        else {
            Config.relations.put(name, tag);
            MainClient.sendClient(Component.literal(String.format("Your relation ship with %s is now \"%s\"", args[1], args[2])).withColor(0xFF00FF00));
        }
        Config.write(Main.MOD_ID);
        return true;
    }

    public static boolean addTagModifier(String cmd) {
        String[] args = cmd.split(" ");
        if (args.length != 3) { // TODO: move error handling out of this, throw InvalidArguments and handle it in cmd.apply
            MainClient.sendClient(Component.literal("Invalid Arguments! Usage: !ctag <relationship> <hex color (#ffffff for white)>"));
            return true;
        }

        Boolean t = switch (args[2].toLowerCase()) {
            case "flip" -> Config.toggleItem(Config.flip, args[1].toLowerCase());
            case "noattack" -> Config.toggleItem(Config.noattack, args[1].toLowerCase());
            default -> {
                MainClient.sendClient(Component.literal("Invalid modifier! Please choose one of: [flip, noattack]")); // FIXME: no manual modifier list pls
                yield null;
            }
        };
        if (t == null) return true;

        MainClient.sendClient(Component.literal(String.format("%s modifier \"%s\" %s relationship tag %s", t ? "Added" : "Removed", args[2], t ? "to" : "from", args[1])).withColor(t ? 0xFFFF00 : 0xFF0000));
        Config.write(Main.MOD_ID);
        return true;
    }

    public static boolean listTags(String cmd) {
        MainClient.sendClient(Component.literal("Here are your created tags:"));
        for (Map.Entry<String, Integer> tag : Config.tags.entrySet()) {
            MainClient.sendClient(Component.literal(tag.getKey()).withColor(tag.getValue()));
        }
        return true;
    }
}
