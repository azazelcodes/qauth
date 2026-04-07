package me.azazeldev.qauth.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.azazeldev.qauth.Config;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler {
    private static HashMap<String, List<Pair<String, ArgumentType<?>>>> cmds = new HashMap<>();
    public static void popCmds() {
        List<Pair<String, ArgumentType<?>>> ctag = List.of( Pair.of("tag name", StringArgumentType.string()), Pair.of("color (as hex)", StringArgumentType.string()) );
        cmds.put("ctag", ctag);
        cmds.put("createtag", ctag);

        List<Pair<String, ArgumentType<?>>> tag = List.of( Pair.of("username", StringArgumentType.string()), Pair.of("tag name", StringArgumentType.string()) );
        cmds.put("tag", tag);
    }
    public static void registerBrigadier(CommandDispatcher<FabricClientCommandSource> dispatcher) { // FIXME: three different warnings, fix plz
        for (Map.Entry<String, List<Pair<String, ArgumentType>>> e :  cmds.entrySet()) {
            ArgumentBuilder<FabricClientCommandSource, LiteralArgumentBuilder<FabricClientCommandSource>> pass0 = ClientCommandManager.literal(e.getKey());
            ArgumentBuilder lpass = null;
            for (Pair<String, ArgumentType> a : e.getValue()) {
                ArgumentBuilder pass = Commands.argument(a.getKey(), a.getValue()).executes(CommandHandler::out);
                if (lpass!=null) pass = pass.then(lpass);
                lpass = pass;
            }
            dispatcher.register(pass0.then(lpass));
        }
    }
    private static int out(CommandContext<FabricClientCommandSource> context) {
        MainClient.sendClient(Component.literal(context.getInput()));
        return (execute("!"+context.getInput()) ? 1 : 0);
    }
    private static List<Pair<String, ArgumentType>> inf(String identifier, ArgumentType type) { return Stream.generate(() -> Pair.of(identifier, type)).limit(16).collect(Collectors.toList()); }

    public static boolean execute(String msg) {
        if (!msg.startsWith("!")) return false;
        Function<String, Boolean> cmd = switch (msg.substring(1).toLowerCase().split(" ")[0]) {
            case "crel", "createrel", "create_rel", "ctag", "createtag" -> CommandHandler::createTag;
            case "tag", "rel" -> CommandHandler::tag;

            case "test" -> CommandHandler::test;

            default -> CommandHandler::EMPTY;
        };
        return cmd.apply(msg);
    }


    private static boolean EMPTY(String cmd) { return false; }

    private static boolean createTag(String cmd) {
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
        Config.tags.put(args[1], 0xFF000000 | col);
        return true;
    }

    private static boolean tag(String cmd) {
        String[] args = cmd.split(" ");
        if (args.length != 3) { // TODO: move error handling out of this, throw InvalidArguments and handle it in cmd.apply
            MainClient.sendClient(Component.literal("Invalid Arguments! Usage: !tag <username> <relationship>"));
            return true;
        }

        boolean isrel = Config.relations.containsKey(args[1]) && Objects.equals(Config.relations.get(args[1]), args[2]);
        Config.relations.remove(args[1]);
        if (isrel) MainClient.sendClient(Component.literal(String.format("Removed %s from %s!", args[1], args[2])).withColor(0xFFFF0000));
        else {
            Config.relations.put(args[1], args[2]);
            MainClient.sendClient(Component.literal(String.format("Your relation ship with %s is now \"%s\"", args[1], args[2])));
        }
        return true;
    }


    private static boolean test(String cmd) {
        //Config.stash.add(new ItemStack(Item.byId(Integer.parseInt(cmd.split(" ")[1])), Integer.parseInt(cmd.split(" ")[2])));
        return true;
    }


    private static boolean addList(List<String> target, String item, String identifier) {
        boolean ran = false;
        for (String n : item.split(" ")) if (!n.startsWith("!")) if (Config.toggleItem(target, n.toLowerCase()))
            { MainClient.sendClient(Component.literal("Added " + n + " to your " + identifier + "!").withColor(0xFF00FF00)); ran=true; }
            else { MainClient.sendClient(Component.literal("Removed " + n + " from your " + identifier + "!").withColor(0xFFFF0000)); ran=true; }
        return ran;
    }
}
