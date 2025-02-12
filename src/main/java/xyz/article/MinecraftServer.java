package xyz.article;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.geysermc.mcprotocollib.network.Server;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.ServerClosedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionRemovedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkBiomeData;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.*;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.chunk.Chunk;
import xyz.article.chunk.ChunkData;
import xyz.article.chunk.ChunkManager;

import java.io.*;
import java.util.*;

public class MinecraftServer {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftServer.class);
    private static final ProxyInfo AUTH_PROXY = null;
    public static final List<Session> playerSessions = new ArrayList<>();
    private static final List<GameProfile> playerProfiles = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new WhenClose();
        File propertiesFile = new File("./settings.yml");
        if (propertiesFile.createNewFile()) logger.info("已创建所需文件");

        Settings.init(propertiesFile);

        SessionService sessionService = new SessionService();
        sessionService.setProxy(AUTH_PROXY);

        Server server = new TcpServer(Settings.BIND_ADDRESS, Settings.SERVER_PORT, MinecraftProtocol::new);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, Settings.ONLINE_MODE);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, Settings.ONLINE_MODE);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session ->
                new ServerStatusInfo(
                        Component.text("§cSlider§bMC §7- §eWelcome!"),
                        new PlayerInfo(Settings.MAX_PLAYERS, playerSessions.size(), playerProfiles),
                        new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
                        null,
                        false
                )
        );

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, session -> {
                    if ((playerSessions.size() + 1) > Settings.MAX_PLAYERS) {
                        session.send(new ClientboundDisconnectPacket("这个服务器没有地方容纳你了！"));
                        return;
                    }
                    GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
                    boolean inServer = false;
                    for (GameProfile profile1 : playerProfiles) {
                        if (profile1.getId().equals(profile.getId())) {
                            inServer = true;
                            break;
                        }
                    }
                    if (inServer) {
                        session.send(new ClientboundDisconnectPacket("你已经在这个服务器里了！"));
                        return;
                    }

                    ProtocolSender.sendBrand("SliderMC", session);
                    session.send(new ClientboundLoginPacket(0, false, new Key[]{Key.key("minecraft:world")}, 0, 16, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:world"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));

                    logger.info("{} 加入了游戏", profile.getName());
                    playerSessions.add(session);
                    Component component = Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : playerSessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                    }
                    playerProfiles.add(profile);

                    session.send(new ClientboundSetChunkCacheCenterPacket(0, 0));
                    for (int x = -4; x < 4; x++) {
                        for (int z = -4; z < 4; z++) {
                            session.send(Chunk.createSimpleGrassChunk(x, z).getChunkPacket());
                        }
                    }
                }
        );

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);
        server.addListener(new ServerAdapter() {
            @Override
            public void serverClosed(ServerClosedEvent event) {
                logger.info("服务器已关闭");
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(Session session, Packet packet) {
                        if (packet instanceof ServerboundChatPacket chatPacket) {
                            GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                            logger.info("{}: {}", profile.getName(), chatPacket.getMessage());
                            Component msg = Component.text("<" + profile.getName() + "> " + chatPacket.getMessage());
                            for (Session session1 : playerSessions) {
                                session1.send(new ClientboundSystemChatPacket(msg, false));
                            }
                        }else if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
                            if (actionPacket.getAction().equals(PlayerAction.FINISH_DIGGING)) {
                                Vector3i blockPos = actionPacket.getPosition();
                                int blockX = blockPos.getX();
                                int blockY = blockPos.getY();
                                int blockZ = blockPos.getZ();
                                int chunkX = blockX >> 4;
                                int chunkZ = blockZ >> 4;
                                int sectionHeight = 16; // 每个section(子区块)的高度
                                int worldBottom = -64; // 世界底部的Y坐标
                                int sectionIndex = (blockY - worldBottom) / sectionHeight;
                                ChunkData chunkData = ChunkManager.chunkDataMap.get(Vector2i.from(chunkX, chunkZ));
                                if (chunkData != null) {
                                    ChunkSection[] chunkSections = chunkData.getChunkSections();
                                    chunkSections[sectionIndex].setBlock(blockX & 15, blockY & 15, blockZ & 15, 0);
                                    ChunkManager.chunkDataMap.get(Vector2i.from(chunkX, chunkZ)).setChunkSections(chunkSections);
                                    session.send(ChunkManager.chunkDataMap.get(Vector2i.from(chunkX, chunkZ)).getChunkPacket());
                                    System.out.println("SectionIndex = " + sectionIndex + ", BlockY = " + blockY);
                                    session.send(new ClientboundBlockChangedAckPacket(actionPacket.getSequence()));
                                } else {
                                    logger.error("Chunk Data (x{}, z{}) is null!", chunkX, chunkZ);
                                }
                            }
                        }else if (packet instanceof ServerboundUseItemOnPacket useItemOnPacket) {
                            Vector3i blockPos = useItemOnPacket.getPosition();
                            int blockX = blockPos.getX();
                            int blockY = blockPos.getY();
                            int blockZ = blockPos.getZ();

                            // 根据玩家点击的面对坐标进行修正
                            switch (useItemOnPacket.getFace()) {
                                case UP:
                                    blockY++;
                                    break;
                                case DOWN:
                                    blockY--;
                                    break;
                                case NORTH:
                                    blockZ--;
                                    break;
                                case SOUTH:
                                    blockZ++;
                                    break;
                                case WEST:
                                    blockX--;
                                    break;
                                case EAST:
                                    blockX++;
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unexpected direction: " + useItemOnPacket.getFace());
                            }

                            // 计算新方块的chunk坐标
                            int chunkX = blockX >> 4;
                            int chunkZ = blockZ >> 4;

                            // 计算新方块的子区块索引
                            int sectionHeight = 16; // 每个section(子区块)的高度
                            int worldBottom = -64; // 世界底部的Y坐标
                            int sectionIndex = (blockY - worldBottom) / sectionHeight;

                            // 获取chunk数据
                            ChunkData chunkData = ChunkManager.chunkDataMap.get(Vector2i.from(chunkX, chunkZ));
                            if (chunkData != null) {
                                ChunkSection[] chunkSections = chunkData.getChunkSections();

                                // 确保子区块索引在有效范围内
                                if (sectionIndex >= 0 && sectionIndex < chunkSections.length) {
                                    // 设置新方块
                                    chunkSections[sectionIndex].setBlock(blockX & 15, blockY & 15, blockZ & 15, 6);
                                    ChunkManager.chunkDataMap.get(Vector2i.from(chunkX, chunkZ)).setChunkSections(chunkSections);

                                    // 发送区块更新包
                                    session.send(new ClientboundSectionBlocksUpdatePacket(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4, new BlockChangeEntry(Vector3i.from(blockX & 15, blockY & 15, blockZ & 15), 6)));
                                    session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));

                                    System.out.println("SectionIndex = " + sectionIndex + ", X = " + blockX + ", Y = " + blockY + ", Z = " + blockZ + ", 新方块应在" + (blockX) + ", " + (blockY) + ", " + (blockZ));
                                } else {
                                    logger.error("Invalid section index: {}", sectionIndex);
                                }
                            } else {
                                logger.error("Chunk Data (x{}, z{}) is null!", chunkX, chunkZ);
                            }
                        }
                    }
                });
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                if (playerSessions.contains(event.getSession())) {
                    GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                    logger.info("{} 离开了游戏", profile.getName());
                    playerSessions.remove(event.getSession());
                    Component component = Component.text(profile.getName() + " 退出了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : playerSessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                    }
                    playerProfiles.remove(profile);
                }
            }
        });

        server.bind();
        logger.info("服务器已在 {}:{} 启动", Settings.BIND_ADDRESS, Settings.SERVER_PORT);
    }
}