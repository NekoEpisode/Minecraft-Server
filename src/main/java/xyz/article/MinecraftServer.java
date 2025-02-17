package xyz.article;

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
import org.geysermc.mcprotocollib.protocol.data.game.advancement.Advancement;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ByteEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundUpdateAdvancementsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheRadiusPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Location;
import xyz.article.api.Slider;
import xyz.article.api.command.CommandSender;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.entity.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.WorldManager;
import xyz.article.api.world.block.ItemToBlock;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.commands.CommandManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MinecraftServer {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftServer.class);
    private static final ProxyInfo AUTH_PROXY = null;
    public static final List<Session> playerSessions = new ArrayList<>();
    private static final List<GameProfile> playerProfiles = new ArrayList<>();
    private volatile static TcpServer server;
    public static World overworld;

    public static void main(String[] args) throws IOException {
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
                    /*int centerX = 0; // 中心点的X坐标
                    int centerY = 0; // 中心点的Y坐标
                    int radius = 10; // 半径，表示从中心点向外的区块数量

                    int startX = centerX - radius;
                    int endX = centerX + radius;
                    int startZ = centerY - radius;
                    int endZ = centerY + radius;

                    int width = endX - startX + 1;
                    int length = endZ - startZ + 1;
                    ChunkData[][] data = new TerrainGenerator(new Random().nextLong(), 0.02).generateTerrain(width, length);

                    for (int i = 0; i < width; i++) {
                        for (int l = 0; l < length; l++) {
                            // 计算实际的区块坐标
                            int chunkX = startX + i;
                            int chunkZ = startZ + l;
                            if (data == null) {
                                session.send(player.getWorld().getChunkDataMap().get(Vector2i.from(chunkX, chunkZ)).getChunkPacket());
                                return;
                            }
                            //session.send(data[i][l].getChunkPacket());
                        }
                    }*/

                    for (int i = -4; i < 4; i++) {
                        for (int q = -4; q < 4; q++) {
                            if (MinecraftServer.overworld.getChunkDataMap().get(Vector2i.from(i, q)) == null) {
                                ChunkData data = Chunk.createSimpleGrassChunk(i, q);
                                MinecraftServer.overworld.getChunkDataMap().put(Vector2i.from(i, q), data);
                                session.send(data.getChunkPacket());
                            }else {
                                session.send(MinecraftServer.overworld.getChunkDataMap().get(Vector2i.from(i, q)).getChunkPacket());
                            }
                        }
                    }

                    session.send(new ClientboundSetChunkCacheRadiusPacket(10));
                    session.send(new ClientboundSetDefaultSpawnPositionPacket(Vector3i.from(8.5, 65, 8.5), 0F));
                    CommandManager.sendPacket(session);

                    Map<String, Map<String, Long>> progress = new HashMap<>();
                    Map<String, Long> welcomeProgress = new HashMap<>();
                    welcomeProgress.put("slider:join", System.currentTimeMillis());
                    progress.put("slider:welcome", welcomeProgress);
                    session.send(new ClientboundUpdateAdvancementsPacket(
                            false,
                            new Advancement[]{
                                    new Advancement(
                                            "slider:welcome",
                                            new ArrayList<>(),
                                            new Advancement.DisplayData(
                                                    Component.text("欢迎来到Slider"),
                                                    Component.text("进入SliderMC服务器"),
                                                    new ItemStack(27),
                                                    Advancement.DisplayData.AdvancementType.CHALLENGE,
                                                    true,
                                                    false,
                                                    0f,
                                                    0f,
                                                    "minecraft:textures/block/dirt.png"
                                            ),
                                            true
                                    )
                            },
                            new String[] { "slider:join"},
                            progress
                    ));

                    /*for (Session session1 : playerSessions) {
                        session1.send(new ClientboundSystemChatPacket(Component.text(profile.getName() + " 完成了挑战").append(Component.text("[欢迎来到Slider]").color(NamedTextColor.DARK_PURPLE)), false));
                    }*/
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
                String[] commands = line.split(" ");
                List<String> args1 = new ArrayList<>(Arrays.asList(commands).subList(1, commands.length));
                final boolean[] executed = new boolean[1];
                Register.getCommandExecutors().forEach((name, executor) -> {
                    if (name.equalsIgnoreCase(commands[0])) {
                        executor.execute(new CommandSender("Console"), args1.toArray(new String[0]));
                        executed[0] = true;
                    }
                });

                if (!executed[0]) {
                    logger.info("未知的命令！");
                }
            }
        }).start();
    }

    public static void stop() {
        logger.info("正在关闭服务器...");
        playerSessions.clear();
        playerProfiles.clear();
        Register.destroy();
        RunningData.playerList.clear();
        WorldManager.worldMap.forEach((key, world) -> world.stop());
        server.close();
        System.exit(0);
    }
}
