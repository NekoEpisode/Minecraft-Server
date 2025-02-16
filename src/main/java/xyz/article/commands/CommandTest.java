package xyz.article.commands;

import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.command.SuggestionType;
import org.geysermc.mcprotocollib.protocol.data.game.command.properties.IntegerProperties;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandSuggestionsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class CommandTest {
    public static void sendPacket(Session session) {
        List<CommandNode> commandNodes = new ArrayList<>();
        CommandNode root = new CommandNode(
                CommandType.ROOT, //根节点
                false, new int[]{ 1 }, //子节点在列表中的索引
                OptionalInt.empty(), //重定向
                "", //命令名称
                null, //解析器，不知道干啥的
                null, //命令Properties
                null); //建议类型
        CommandNode test = new CommandNode(CommandType.LITERAL, true, new int[]{}, OptionalInt.empty(), "gamemode", CommandParser.MESSAGE, new IntegerProperties(0, 1), SuggestionType.ALL_RECIPES.getResourceLocation());
        commandNodes.add(root);
        commandNodes.add(test);

        session.send(new ClientboundCommandsPacket(commandNodes.toArray(new CommandNode[0]), 0));
    }
}
