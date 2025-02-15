package xyz.article;

import io.netty.handler.codec.base64.Base64Decoder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.*;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.ProxyInfo;
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
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundKeepAlivePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerAbilitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHealthPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundChunksBiomesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheRadiusPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Location;
import xyz.article.api.Slider;
import xyz.article.api.entity.player.PlayerInfoAction;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.entity.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.WorldManager;
import xyz.article.api.world.block.ItemToBlock;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MinecraftServer {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftServer.class);
    private static final ProxyInfo AUTH_PROXY = null;
    public static final List<Session> playerSessions = new ArrayList<>();
    private static final List<GameProfile> playerProfiles = new ArrayList<>();
    private static TcpServer server;
    public static World overworld;
    private final static Map<ChunkPos, ChunkData> cache = new HashMap<>();

    public static void main(String[] args) throws IOException {
        new WhenClose();
        Thread.currentThread().setName("Main Thread");
        File propertiesFile = new File("./settings.yml");
        if (propertiesFile.createNewFile()) logger.info("已创建所需文件");

        Settings.init(propertiesFile);
        Register.registerALL();
        ItemToBlock.writeMap();

        SessionService sessionService = new SessionService();
        sessionService.setProxy(AUTH_PROXY);

        overworld = new World(Key.key("minecraft:overworld"));

        server = new TcpServer(Settings.BIND_ADDRESS, Settings.SERVER_PORT, MinecraftProtocol::new);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, Settings.ONLINE_MODE);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, Settings.ONLINE_MODE);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session -> new ServerStatusInfo(
                Component.text("§cSlider§bMC §7- §eWelcome!"),
                new PlayerInfo(Settings.MAX_PLAYERS, playerSessions.size(), playerProfiles),
                new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
                null,
                false
        ));

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, session -> {
                    // 检查玩家是否可以加入游戏
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

                    // 发送所有登录数据包
                    ProtocolSender.sendBrand("SliderMC", session); // 发送服务器品牌(服务器名称)
                    //ProtocolSender.sendCommands(session); // 发送命令
                    List<Key> worldNames = new ArrayList<>();
                    WorldManager.worldMap.forEach((key, world) -> worldNames.add(key));
                    int entityID = new Random().nextInt(0, 999999999);
                    session.send(new ClientboundLoginPacket(entityID, false, worldNames.toArray(new Key[0]), 0, 10, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:overworld"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));
                    logger.info("{} 加入了游戏", profile.getName());
                    Component component = Component.text(profile.getName() + " 加入了游戏, EntityID = " + entityID).color(NamedTextColor.YELLOW);
                    Inventory inventory = new Inventory(profile.getName() + "'s Inventory", ContainerType.GENERIC_9X6, 47, -1);
                    Player player = new Player(profile, session, entityID, GameMode.CREATIVE, inventory, overworld, new Location(overworld, Vector3d.from(8.5, 65, 8.5)), Vector2f.from(0, 0));
                    RunningData.playerList.add(player);
                    session.send(ProtocolSender.getPlayerInfoPacketALL());
                    session.send(new ClientboundContainerSetContentPacket(inventory.getContainerId(), 0, inventory.getItems(), null));
                    for (Session session1 : playerSessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                        session1.send(ProtocolSender.getPlayerInfoPacketAdd(player));
                        session1.send(new ClientboundAddEntityPacket(player.getEntityID(), profile.getId(), EntityType.PLAYER, player.getLocation().pos().getX(), player.getLocation().pos().getY(), player.getLocation().pos().getZ(), player.getAngle().getX(), player.getAngle().getY(), 0));
                    }
                    for (Player player1 : RunningData.playerList) {
                        if (!player1.equals(player)) {
                            session.send(new ClientboundAddEntityPacket(player1.getEntityID(), player1.getProfile().getId(), EntityType.PLAYER, player1.getLocation().pos().getX(), player1.getLocation().pos().getY(), player1.getLocation().pos().getZ(), player1.getAngle().getX(), player1.getAngle().getY(), 0));
                        }
                    }
                    playerSessions.add(session);
                    playerProfiles.add(profile);

                    session.send(new ClientboundSetChunkCacheCenterPacket(0, 0));
                    PerlinNoise noise = new PerlinNoise(12345L);
                    int centerX = 8; // 中心点的X坐标
                    int centerY = 8; // 中心点的Y坐标
                    int radius = 5; // 半径，表示从中心点向外的区块数量

                    int startX = centerX - radius;
                    int endX = centerX + radius;
                    int startZ = centerY - radius;
                    int endZ = centerY + radius;

                    int width = endX - startX + 1;
                    int length = endZ - startZ + 1;
                    ChunkData[][] data = noise.generateTerrain(width, length);

                    for (int i = 0; i < width; i++) {
                        for (int l = 0; l < length; l++) {
                            // 计算实际的区块坐标
                            int chunkX = startX + i;
                            int chunkZ = startZ + l;
                            ChunkPos chunkPos = new ChunkPos(overworld, Vector2i.from(chunkX, chunkZ));

                            if (cache.get(chunkPos) == null) {
                                ChunkData chunkData = data[i][l];
                                cache.put(chunkPos, chunkData);
                                session.send(chunkData.getChunkPacket());
                            } else {
                                ChunkData chunkData1 = cache.get(chunkPos);
                                session.send(chunkData1.getChunkPacket());
                            }
                        }
                    }

                    session.send(new ClientboundSetChunkCacheRadiusPacket(10));
                    session.send(new ClientboundSetDefaultSpawnPositionPacket(Vector3i.from(8.5, 65, 8.5), 0F));
                }
        );

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);
        server.addListener(new ServerAdapter() {
            @Override
            public void serverClosed(ServerClosedEvent event) {
                playerSessions.clear();
                playerProfiles.clear();
                RunningData.playerList.clear();
                WorldManager.worldMap.forEach((key, world) -> world.save());

                logger.info("服务器已关闭");
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(Session session, Packet packet) {
                        /*if (!(packet instanceof ServerboundKeepAlivePacket)) {
                            logger.info(packet.toString());
                        }*/

                        // 交给处理器去处理
                        for (PacketProcessor processor : Register.getPacketProcessors()) {
                            processor.process(packet, session);
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
                        session1.send(new ClientboundRemoveEntitiesPacket(new int[]{Objects.requireNonNull(Slider.getPlayer(event.getSession())).getEntityID()}));
                        session1.send(new ClientboundPlayerInfoRemovePacket(List.of(profile.getId())));
                    }
                    playerProfiles.remove(profile);
                    RunningData.playerList.remove(Slider.getPlayer(event.getSession()));
                }
            }
        });

        server.bind();
        logger.info("服务器已在 {}:{} 启动", Settings.BIND_ADDRESS, Settings.SERVER_PORT);

        new Thread(() -> {
            Thread.currentThread().setName("Input Thread");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if (line.startsWith("say")) {
                    String[] commands = line.split(" ");
                    if (commands.length > 1) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < commands.length; i++) {
                            builder.append(commands[i]);
                            if (i != commands.length - 1) {
                                builder.append(" ");
                            }
                        }
                        for (Session session : playerSessions) {
                            session.send(new ClientboundSystemChatPacket(Component.text("<Server> " + builder), false));
                        }
                        logger.info("{}: {}", "Server", builder);
                    }else {
                        logger.warn("需要更多参数！");
                    }
                }
            }
        }).start();
    }

    public static TcpServer getServer() {
        return server;
    }
}