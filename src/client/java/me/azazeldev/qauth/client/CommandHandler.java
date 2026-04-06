package me.azazeldev.qauth.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.azazeldev.qauth.Config;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler {
    private static HashMap<String, List<Pair<String, ArgumentType>>> cmds = new HashMap<>();
    public static void popCmds() {
        List<Pair<String,ArgumentType>> plist = inf("player", EntityArgument.player());
        cmds.put("war", plist);
        cmds.put("enemy", plist);

        cmds.put("team", plist);
        cmds.put("friend", plist);
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
            case "tag", "rel" -> CommandHandler::tag;

            case "test" -> CommandHandler::test;

            default -> CommandHandler::EMPTY;
        };
        return cmd.apply(msg);
    }


    private static boolean EMPTY(String cmd) { return false; }

    private static boolean tag(String cmd) {
        String[] args = cmd.split(" ");
        if (args.length != 2) { // TODO: move error handling out of this, throw InvalidArguments and handle it in cmd.apply
            MainClient.sendClient(Component.literal("Invalid Arguments! Usage: !tag <username> <relationship>"));
            return false;
        }

        Config.relations.remove(args[0]);
        if (Config.relations.containsKey(args[0]) && Objects.equals(Config.relations.get(args[0]), args[1])) MainClient.sendClient(Component.literal(String.format("Removed %s from %s!", args[0], args[1])).withColor(0xFFFF0000));
        else {
            Config.relations.put(args[0], args[1]);
            MainClient.sendClient(Component.literal(String.format("Your relation ship with %s is now \"%s\"", args[0], args[1])));
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
