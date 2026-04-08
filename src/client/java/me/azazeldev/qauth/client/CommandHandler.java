package me.azazeldev.qauth.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.client.commands.TagCommands;
import me.azazeldev.qauth.client.gui.QuestTracker;
import me.azazeldev.qauth.client.gui.RelationshipManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler {
    private static HashMap<String, List<Pair<String, ArgumentType<?>>>> cmds = new HashMap<>();
    public static void popCmds() {
        List<Pair<String, ArgumentType<?>>> ctag = List.of( Pair.of("tag name", StringArgumentType.string()), Pair.of("color (as hex)", StringArgumentType.string()) );
        cmds.put("ctag", ctag);
        cmds.put("tagcreate", ctag);

        List<Pair<String, ArgumentType<?>>> dtag = List.of( Pair.of("tag name", StringArgumentType.string()) );
        cmds.put("dtag", dtag);
        cmds.put("tagdelete", dtag);

        List<Pair<String, ArgumentType<?>>> modtag = List.of( Pair.of("tag name", StringArgumentType.string()), Pair.of("modifier", StringArgumentType.string()) ); // TODO: move modifier to an enum
        cmds.put("tagmodifier", modtag);

        List<Pair<String, ArgumentType<?>>> tag = List.of( Pair.of("username", EntityArgument.player()), Pair.of("tag name", StringArgumentType.string()) );
        cmds.put("tag", tag);

        cmds.put("tags", null);


        cmds.put("quests", null);
    }
    public static void registerBrigadier(CommandDispatcher<FabricClientCommandSource> dispatcher) { // FIXME: three different warnings, fix plz
        for (Map.Entry<String, List<Pair<String, ArgumentType<?>>>> e :  cmds.entrySet()) {
            ArgumentBuilder<FabricClientCommandSource, LiteralArgumentBuilder<FabricClientCommandSource>> pass0 = ClientCommandManager.literal(e.getKey());
            if (e.getValue() == null) {
                dispatcher.register(pass0.executes(CommandHandler::out));
                continue;
            }
            ArgumentBuilder lpass = null;
            for (Pair<String, ArgumentType<?>> a : e.getValue()) {
                ArgumentBuilder pass = Commands.argument(a.getKey(), a.getValue()).executes(CommandHandler::out);
                if (lpass!=null) pass = lpass.then(pass);
                lpass = pass;
            }
            dispatcher.register(pass0.then(lpass));
        }
    }
    public static boolean isCmd(String s) { return cmds.containsKey(s); };
    private static int out(CommandContext<?> context) {
        return (execute("!"+context.getInput()) ? 1 : 0);
    }
    private static List<Pair<String, ArgumentType>> inf(String identifier, ArgumentType type) { return Stream.generate(() -> Pair.of(identifier, type)).limit(16).collect(Collectors.toList()); }

    public static boolean execute(String msg) {
        if (!msg.startsWith("!")) return false;
        Function<String, Boolean> cmd = switch (msg.substring(1).toLowerCase().split(" ")[0]) {
            case "crel", "createrel", "ctag", "createtag", "tagcreate" -> TagCommands::createTag;
            case "drel", "deleterel", "dtag", "deletetag", "tagdelete" -> TagCommands::deleteTag;
            case "tag", "rel" -> TagCommands::tag;
            case "modtag", "modrel", "tagmodifier" -> TagCommands::addTagModifier;
            case "tags", "rels" -> RelationshipManager::showGUI;

            case "test" -> CommandHandler::test;

            case "quests" -> QuestTracker::showGUI;

            default -> CommandHandler::EMPTY;
        };
        return cmd.apply(msg);
    }

    private static boolean EMPTY(String cmd) { return false; }

    private static boolean test(String cmd) {
        Minecraft.getInstance().player.connection.sendChat("/stash");
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
