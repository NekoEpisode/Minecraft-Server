package xyz.article;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class MinecraftServer {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftServer.class);
    private static final ProxyInfo AUTH_PROXY = null;
    public static final List<Session> playerSessions = new ArrayList<>();
    private static final List<GameProfile> playerProfiles = new ArrayList<>();

    public static void main(String[] args) throws IOException {
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

                    ProtocolSender.sendBrand("Slider", session);
                    session.send(new ClientboundLoginPacket(0, false, new Key[]{Key.key("minecraft:world")}, 0, 16, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:world"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));

                    logger.info("{} 加入了游戏", profile.getName());
                    playerSessions.add(session);
                    Component component = Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : playerSessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                    }
                    playerProfiles.add(profile);
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
                            if (chatPacket.getMessage().startsWith(".tp")) {
                                session.send(new ClientboundPlayerPositionPacket(
                                        0D,
                                        64D,
                                        0D,
                                        0F,
                                        0F,
                                        1
                                ));
                                return;
                            }
                            GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                            logger.info("{}: {}", profile.getName(), chatPacket.getMessage());
                            Component msg = Component.text("<" + profile.getName() + "> " + chatPacket.getMessage());
                            for (Session session1 : playerSessions) {
                                session1.send(new ClientboundSystemChatPacket(msg, false));
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