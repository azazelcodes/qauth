package me.azazeldev.qauth.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.azazeldev.qauth.client.gui.QuestTracker;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
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
    public static boolean isCmd(String s) { return cmds.containsKey(s); }

    private static int out(CommandContext<?> context) {
        return (execute("!"+context.getInput()) ? 1 : 0);
    }
    private static List<Pair<String, ArgumentType>> inf(String identifier, ArgumentType type) { return Stream.generate(() -> Pair.of(identifier, type)).limit(16).collect(Collectors.toList()); }

    public static boolean execute(String msg) {
        if (!msg.startsWith("!")) return false;
        Function<String, Boolean> cmd = switch (msg.substring(1).toLowerCase().split(" ")[0]) {
            /*case "crel", "createrel", "ctag", "createtag", "tagcreate" -> TagCommands::createTag;
            case "drel", "deleterel", "dtag", "deletetag", "tagdelete" -> TagCommands::deleteTag;
            case "tag", "rel" -> TagCommands::tag;
            case "modtag", "modrel", "tagmodifier" -> TagCommands::addTagModifier;
            case "tags", "rels" -> RelationshipManager::showGUI;*/

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
}
