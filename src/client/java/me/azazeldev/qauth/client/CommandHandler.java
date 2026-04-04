package me.azazeldev.qauth.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.azazeldev.qauth.Config;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            ArgumentBuilder<FabricClientCommandSource, LiteralArgumentBuilder<FabricClientCommandSource>> pass0 = ClientCommands.literal(e.getKey());
            ArgumentBuilder lpass = null;
            int i = 0;
            for (Pair<String, ArgumentType> a : e.getValue()) {
                ArgumentBuilder pass = Commands.argument(a.getKey(), a.getValue()).executes(CommandHandler::out);
                if (lpass!=null) pass = pass.then(lpass);
                lpass = pass;
                i++;
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
        Function<String, Boolean> cmd = switch (msg.substring(1).split(" ")[0]) {
            case "war", "enemy" -> CommandHandler::war;
            case "team", "friend" -> CommandHandler::team;

            case "test" -> CommandHandler::test;

            default -> CommandHandler::EMPTY;
        };
        return cmd.apply(msg);
    }


    private static boolean EMPTY(String cmd) { return false; }

    private static boolean team(String cmd) { return addList(Config.tm8s, cmd, "team"); }
    private static boolean war(String cmd) { return addList(Config.wars, cmd, "wars"); }


    private static boolean test(String cmd) {
        Config.stash.add(new ItemStack(Item.byId(Integer.parseInt(cmd.split(" ")[1])), Integer.parseInt(cmd.split(" ")[2])));
        return true;
    }


    private static boolean addList(List<String> target, String item, String identifier) {
        boolean ran = false;
        for (String n : item.split(" ")) if (!n.startsWith("!")) if (Config.toggleItem(target, n))
            { MainClient.sendClient(Component.literal("Added " + n + " to your " + identifier + "!").withColor(0xFF00FF00)); ran=true; }
            else { MainClient.sendClient(Component.literal("Removed " + n + " from your " + identifier + "!").withColor(0xFFFF0000)); ran=true; }
        return ran;
    }
}
