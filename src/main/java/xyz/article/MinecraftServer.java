package xyz.article;

import io.netty.handler.codec.base64.Base64Decoder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
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
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
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
                    List<Key> worldNames = new ArrayList<>();
                    WorldManager.worldMap.forEach((key, world) -> worldNames.add(key));
                    session.send(new ClientboundLoginPacket(0, false, worldNames.toArray(new Key[0]), 0, 16, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:overworld"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));
                    session.send(new ClientboundSetDefaultSpawnPositionPacket(Vector3i.from(0, 1, 0), 0F));

                    Player player = null;
                    for (Player player1 : RunningData.playerList) {
                        if (player1.getProfile().getId().equals(profile.getId())) {
                            player = player1;
                            break;
                        }
                    }

                    if (player != null) {
                        Inventory inventory = player.getInventory();
                        session.send(new ClientboundContainerSetContentPacket(inventory.getContainerId(), 0, inventory.getItems(), player.getInventory().getItem(46)));
                    }else {
                        Inventory inventory = new Inventory(profile.getName() + "'s Inventory", ContainerType.GENERIC_9X6, 47, -1); // 为此玩家新生成一个物品栏
                        player = new Player(profile, session, new Random().nextInt(), GameMode.CREATIVE, inventory, overworld);
                        RunningData.playerList.add(player);
                        session.send(new ClientboundContainerSetContentPacket(inventory.getContainerId(), 0, inventory.getItems(), null));
                    }

                    logger.info("{} 加入了游戏", profile.getName());
                    ClientboundPlayerInfoUpdatePacket packet = ProtocolSender.getPlayerInfoPacketAdd(player);
                    Component component = Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : playerSessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                        session1.send(new ClientboundAddEntityPacket(player.getEntityID(), profile.getId(), EntityType.PLAYER, 0d, 0d, 1d, 0, 0, 0));
                        session1.send(packet);
                    }
                    session.send(ProtocolSender.getPlayerInfoPacketALL());
                    playerSessions.add(session);
                    playerProfiles.add(profile);

                    session.send(new ClientboundSetChunkCacheCenterPacket(0, 0));
                    for (int x = -6; x < 6; x++) {
                        for (int z = -6; z < 6; z++) {
                            session.send(Chunk.createSimpleGrassChunk(x, z).getChunkPacket());
                        }
                    }
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
                        if (!(packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundMovePlayerPosPacket || packet instanceof ServerboundMovePlayerPosRotPacket || packet instanceof ServerboundMovePlayerRotPacket)) {
                            logger.info(packet.toString());
                        }

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
                        session1.send(new ClientboundPlayerInfoRemovePacket(List.of(profile.getId())));
                    }
                    playerProfiles.remove(profile);
                }
            }
        });

        server.bind();
        logger.info("服务器已在 {}:{} 启动", Settings.BIND_ADDRESS, Settings.SERVER_PORT);
    }

    public static TcpServer getServer() {
        return server;
    }
}