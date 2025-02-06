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
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MinecraftServer {
    private static final Logger log = LoggerFactory.getLogger(MinecraftServer.class);
    private static final boolean ENCRYPT_CONNECTION = false;
    private static final boolean SHOULD_AUTHENTICATE = false;
    private static final ProxyInfo AUTH_PROXY = null;
    private static final List<Session> sessions = new ArrayList<>();
    private static final List<GameProfile> players = new ArrayList<>();

    public static void main(String[] args) {
        SessionService sessionService = new SessionService();
        sessionService.setProxy(AUTH_PROXY);

        Server server = new TcpServer("127.0.0.1", 25565, MinecraftProtocol::new);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, ENCRYPT_CONNECTION);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, SHOULD_AUTHENTICATE);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session ->
                new ServerStatusInfo(
                        Component.text("§cSlider§bMC §7- §eWelcome!"),
                        new PlayerInfo(100, sessions.size(), players),
                        new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
                        null,
                        false
                )
        );

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, session -> {
                    session.send(new ClientboundLoginPacket(
                            0,
                            false,
                            new Key[]{Key.key("minecraft:world")},
                            0,
                            16,
                            16,
                            false,
                            false,
                            false,
                            new PlayerSpawnInfo(
                                    0,
                                    Key.key("minecraft:world"),
                                    100,
                                    GameMode.SURVIVAL,
                                    GameMode.SURVIVAL,
                                    false,
                                    false,
                                    null,
                                    100
                            ),
                            true
                    ));
                    GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
                    log.info("{} 加入了游戏", profile.getName());
                    sessions.add(session);
                    Component component = Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : sessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                    }
                    players.add(profile);
                }
        );

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);
        server.addListener(new ServerAdapter() {
            @Override
            public void serverClosed(ServerClosedEvent event) {
                log.info("服务器已关闭");
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(Session session, Packet packet) {
                        if (packet instanceof ServerboundChatPacket chatPacket) {
                            GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                            log.info("{}: {}", profile.getName(), chatPacket.getMessage());
                            Component msg = Component.text("<" + profile.getName() + "> " + chatPacket.getMessage());
                            for (Session session1 : sessions) {
                                session1.send(new ClientboundSystemChatPacket(msg, false));
                            }
                        }
                    }
                });
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                log.info("{} 离开了游戏", profile.getName());
                sessions.remove(event.getSession());
                Component component = Component.text(profile.getName() + " 退出了游戏").color(NamedTextColor.YELLOW);
                for (Session session1 : sessions) {
                    session1.send(new ClientboundSystemChatPacket(component, false));
                }
                players.remove(profile);
            }
        });

        server.bind();
    }
}