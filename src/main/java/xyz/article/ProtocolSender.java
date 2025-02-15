package xyz.article;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import xyz.article.api.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProtocolSender {
    public static void sendBrand(String brandName, Session session) {
        ByteBuf buf = Unpooled.buffer(brandName.length() + 1);
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        helper.writeVarInt(buf, brandName.length());
        buf.writeBytes(brandName.getBytes(StandardCharsets.UTF_8));
        session.send(new ClientboundCustomPayloadPacket(Key.key("minecraft:brand"), buf.array()));
    }

    public static ClientboundPlayerInfoUpdatePacket getPlayerInfoPacketAdd(Player player) {
        EnumSet<PlayerListEntryAction> actions = EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_LISTED);
        return new ClientboundPlayerInfoUpdatePacket(actions, new PlayerListEntry[]{ new PlayerListEntry(
                player.getProfile().getId(),
                player.getProfile(),
                true,
                0,
                GameMode.CREATIVE,
                null,
                UUID.randomUUID(),
                -1,
                null,
                null
        )});
    }

    public static ClientboundPlayerInfoUpdatePacket getPlayerInfoPacketALL() {
        List<PlayerListEntry> list = new ArrayList<>();
        for (Player player1 : RunningData.playerList) {
            list.add(new PlayerListEntry(
                    player1.getProfile().getId(),
                    player1.getProfile(),
                    true,
                    0,
                    GameMode.CREATIVE,
                    null,
                    UUID.randomUUID(),
                    -1,
                    null,
                    null
            ));
        }
        EnumSet<PlayerListEntryAction> actions1 = EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_LISTED);
        return new ClientboundPlayerInfoUpdatePacket(actions1, list.toArray(new PlayerListEntry[0]));
    }

    public static void sendCommands(Session session) {
        // 创建根节点
        CommandNode rootNode = new CommandNode(
                CommandType.ROOT, // 根节点类型
                false,           // 不可执行
                new int[]{1},    // 子节点索引（test 命令节点的索引）
                OptionalInt.empty(), // 无重定向
                "",             // 根节点无名称
                null,           // 无解析器
                null,           // 无属性
                null            // 无建议类型
        );

        // 创建 test 命令节点
        CommandNode testNode = new CommandNode(
                CommandType.LITERAL, // 字面量类型（固定命令名）
                true,               // 可执行
                new int[]{2, 3},    // 子节点索引（sub 和 arg1 的索引）
                OptionalInt.empty(), // 无重定向
                "test",            // 命令名称
                null,             // 无解析器
                null,             // 无属性
                null              // 无建议类型
        );

        // 创建 sub 子命令节点
        CommandNode subNode = new CommandNode(
                CommandType.LITERAL, // 字面量类型
                true,               // 可执行
                new int[]{},        // 无子节点
                OptionalInt.empty(), // 无重定向
                "sub",             // 子命令名称
                null,              // 无解析器
                null,              // 无属性
                null               // 无建议类型
        );

        // 创建 arg1 参数节点
        CommandNode arg1Node = new CommandNode(
                CommandType.ARGUMENT, // 参数类型
                true,                // 可执行
                new int[]{},         // 无子节点
                OptionalInt.empty(), // 无重定向
                "arg1",             // 参数名称
                CommandParser.STRING, // 参数解析器（字符串类型）
                null,              // 无属性
                null               // 无建议类型
        );

        // 将所有节点放入数组
        CommandNode[] commandNodes = new CommandNode[]{
                rootNode, // 索引 0
                testNode, // 索引 1
                subNode,  // 索引 2
                arg1Node  // 索引 3
        };

        // 创建命令包
        ClientboundCommandsPacket clientboundCommandsPacket = new ClientboundCommandsPacket(
                commandNodes, // 命令节点数组
                0            // 根节点索引
        );

        // 发送命令包
        session.send(clientboundCommandsPacket);
    }
}
