package xyz.article;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import xyz.article.api.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

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
}
