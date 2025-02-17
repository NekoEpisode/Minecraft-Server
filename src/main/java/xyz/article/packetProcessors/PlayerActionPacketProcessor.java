package xyz.article.packetProcessors;

import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Location;
import xyz.article.api.Slider;
import xyz.article.api.entity.Entity;
import xyz.article.api.entity.ItemEntity;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.entity.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

import java.util.Random;
import java.util.UUID;

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

                case DROP_ITEM -> {
                    Player player = Slider.getPlayer(session);
                    if (player != null) {
                        ItemStack itemStack = player.getMainHand().getCurrentItem();
                        if (itemStack != null) {
                            Location location = player.getLocation();
                            int id = new Random().nextInt();
                            UUID uuid = UUID.randomUUID();
                            double x = location.pos().getX();
                            double y = location.pos().getY();
                            double z = location.pos().getZ();

                            // 计算玩家视线方向的速度向量
                            float yaw = player.getAngle().getX();
                            float pitch = player.getAngle().getY();
                            double velocityX = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                            double velocityY = -Math.sin(Math.toRadians(pitch));
                            double velocityZ = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

                            // 应用速度系数
                            double speed = 5.5;
                            velocityX *= speed;
                            velocityY *= speed;
                            velocityY += 0.2;
                            velocityZ *= speed;

                            // 发送生成物品实体的数据包
                            Entity entity = new ItemEntity(new Location(player.getWorld(), Vector3d.from(x, y, z)), EntityType.ITEM, id, itemStack);
                            player.getWorld().getEntities().add(entity);
                            session.send(new ClientboundAddEntityPacket(id, uuid, EntityType.ITEM, x, y, z, 0, 0, 0));
                            session.send(new ClientboundSetEntityDataPacket(id, new EntityMetadata[] {
                                    new ObjectEntityMetadata<>(8, MetadataType.ITEM, itemStack)
                            }));
                        }
                    }
                }

            }
        }
    }
}
