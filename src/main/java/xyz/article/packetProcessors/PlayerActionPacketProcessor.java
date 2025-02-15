package xyz.article.packetProcessors;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.entity.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

public class PlayerActionPacketProcessor implements PacketProcessor {

    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
            switch (actionPacket.getAction()) {
                case START_DIGGING -> {
                    Player player = Slider.getPlayer(session);
                    if (player != null && player.getGameMode().equals(GameMode.CREATIVE)) {
                        // In Creative Mode
                        World world = player.getWorld();
                        BlockPos blockPos = new BlockPos(world, actionPacket.getPosition());
                        ChunkPos chunkPos = Slider.getChunkPos(blockPos);
                        int chunkSectionIndex = Slider.getChunkSectionIndex(blockPos.pos().getY());
                        ChunkData chunk = world.getChunkDataMap().get(chunkPos.pos());
                        ChunkSection[] chunkSections = chunk.getChunkSections();
                        ChunkSection targetSection = chunkSections[chunkSectionIndex];
                        Vector3i inChunkSectionLocation = Slider.getInChunkSectionLocation(blockPos.pos().getX(), blockPos.pos().getY(), blockPos.pos().getZ(), chunkSectionIndex);
                        targetSection.setBlock(inChunkSectionLocation.getX(), inChunkSectionLocation.getY(), inChunkSectionLocation.getZ(), 0);
                        session.send(new ClientboundBlockChangedAckPacket(actionPacket.getSequence()));
                        for (Session session1 : MinecraftServer.playerSessions) {
                            session1.send(chunk.getChunkPacket());
                            if (!(session1.equals(session))) {
                                session1.send(new ClientboundAnimatePacket(player.getEntityID(), Animation.SWING_ARM));
                            }
                        }
                    }
                }

                case FINISH_DIGGING -> {
                    Player player = Slider.getPlayer(session);
                    if (player != null) {
                        World world = player.getWorld();
                        BlockPos blockPos = new BlockPos(world, actionPacket.getPosition());
                        ChunkPos chunkPos = Slider.getChunkPos(blockPos);
                        int chunkSectionIndex = Slider.getChunkSectionIndex(blockPos.pos().getY());
                        ChunkData chunk = world.getChunkDataMap().get(chunkPos.pos());
                        ChunkSection[] chunkSections = chunk.getChunkSections();
                        ChunkSection targetSection = chunkSections[chunkSectionIndex];
                        Vector3i inChunkSectionLocation = Slider.getInChunkSectionLocation(blockPos.pos().getX(), blockPos.pos().getY(), blockPos.pos().getZ(), chunkSectionIndex);
                        targetSection.setBlock(inChunkSectionLocation.getX(), inChunkSectionLocation.getY(), inChunkSectionLocation.getZ(), 0);
                        session.send(new ClientboundBlockChangedAckPacket(actionPacket.getSequence()));
                        for (Session session1 : MinecraftServer.playerSessions) {
                            session1.send(chunk.getChunkPacket());
                        }
                    }
                }
            }
        }
    }
}
