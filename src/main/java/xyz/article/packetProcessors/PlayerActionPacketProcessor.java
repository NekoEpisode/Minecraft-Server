package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkPos;

public class PlayerActionPacketProcessor implements PacketProcessor {

    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
            switch (actionPacket.getAction()) {
                case START_DIGGING -> {
                    Player player = Slider.getPlayer(session);
                    if (player != null && player.getGameMode().equals(GameMode.CREATIVE)) {
                        World world = player.getWorld();
                        BlockPos blockPos = new BlockPos(world, actionPacket.getPosition());
                        ChunkPos chunkPos = Slider.getChunkPos(blockPos);

                    }
                }

                case FINISH_DIGGING -> {

                }
            }
        }
    }
}
