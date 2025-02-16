package xyz.article.commands;

import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.command.SuggestionType;
import org.geysermc.mcprotocollib.protocol.data.game.command.properties.IntegerProperties;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class CommandTest {
    public static void sendPacket(Session session) {
        List<CommandNode> commandNodes = new ArrayList<>();
        CommandNode root = new CommandNode(CommandType.ROOT, false, new int[]{ 1 }, OptionalInt.empty(), "", null, null, null);
        CommandNode test = new CommandNode(CommandType.LITERAL, true, new int[]{}, OptionalInt.empty(), "gamemode", CommandParser.MESSAGE, new IntegerProperties(0, 1), SuggestionType.ASK_SERVER.getResourceLocation());
        commandNodes.add(root);
        commandNodes.add(test);

        session.send(new ClientboundCommandsPacket(commandNodes.toArray(new CommandNode[0]), 0));
    }
}
