package xyz.article.commands;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.command.properties.IntegerProperties;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;
import xyz.article.Register;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class CommandManager {
    public static void sendPacket(Session session) {
        List<CommandNode> commandNodes = new ArrayList<>();
        List<CommandNode> cache = new ArrayList<>();
        List<Integer> index = new ArrayList<>();
        int count = 0;

        for (String name : Register.getCommandExecutors().keySet()) {
            count++;
            CommandNode node = new CommandNode(
                    CommandType.LITERAL,
                    true,
                    new int[]{},
                    OptionalInt.empty(),
                    name,
                    null,
                    new IntegerProperties(0, 1),
                    null
            );
            index.add(count);
            cache.add(node);
        }

        int[] childIndices = new int[index.size()];
        for (int i = 0; i < index.size(); i++) {
            childIndices[i] = index.get(i);
        }

        CommandNode root = new CommandNode(
                CommandType.ROOT, //根节点
                false, //是否可执行
                childIndices, //子节点在列表中的索引
                OptionalInt.empty(), //重定向
                "", //命令名称
                null, //解析器，不知道干啥的
                null, //命令Properties
                null //建议类型
        );
        commandNodes.add(root);

        commandNodes.addAll(cache);

        session.send(new ClientboundCommandsPacket(commandNodes.toArray(new CommandNode[0]), 0));
    }
}