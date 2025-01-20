package xyz.article;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import xyz.article.protocol.ConnectionManager;
import xyz.article.protocol.ProtocolUtils;
import xyz.article.protocol.packets.handshake.HandshakePacket;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleMinecraftServer {

    private static final int PORT = 25565;
    private static final String MOTD = "§4Sli§cder§bMC";

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        System.out.println("Server starting on " + PORT);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 1, 0, 1));
                            p.addLast(new LengthFieldPrepender(1));
                            p.addLast(new MinecraftServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    private static class MinecraftServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private static final ConnectionManager connectionStateManager = new ConnectionManager();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            System.out.println("-----新一轮请求----");
            SocketAddress address = ctx.channel().remoteAddress();
            int packetId = ProtocolUtils.readVarInt(msg);
            System.out.println("Packet ID: " + packetId);
            if (packetId == 0x01) {
                long time = msg.readLong();
                ByteBuf response = ctx.alloc().buffer();
                ProtocolUtils.writeVarInt(response, 0x01);
                response.writeLong(time);
                ctx.writeAndFlush(response);
                System.out.println("Pong returned.");
            }

            if (packetId == 0x00) { // Handshake packet
                System.out.println("接收到握手包！");
                if (connectionStateManager.getClientState(address) == 0) {
                    HandshakePacket handshakePacket = new HandshakePacket();
                    handshakePacket.decode(msg);

                    System.out.println("协议版本: " + handshakePacket.getProtocolVersion());
                    System.out.println("服务器地址: " + handshakePacket.getServerAddress());
                    System.out.println("服务器端口: " + handshakePacket.getServerPort());
                    System.out.println("下一个状态: " + handshakePacket.getNextState());

                    connectionStateManager.setClientState(address, handshakePacket.getNextState());
                    return;
                }
                if (connectionStateManager.getClientState(address) == 1) {
                    ByteBuf response = ctx.alloc().buffer();
                    ProtocolUtils.writeVarInt(response, 0x00);
                    ByteBuf motdJson = ctx.alloc().buffer();
                    Map<String, Object> data = new HashMap<>();
                    Gson gson = new Gson();
                    Map<Object, Object> des = new HashMap<>();
                    des.put("text", MOTD);
                    data.put("description", des);
                    Map<Object, Object> players = new HashMap<>();
                    players.put("max", 100);
                    players.put("online", RunningData.people);
                    data.put("players", players); //我去吃个饭，你研究下
                    Map<Object, Object> version = new HashMap<>();
                    version.put("name", "1.21.4");
                    version.put("protocol", 769);
                    data.put("version", version);
                    motdJson.writeBytes(gson.toJson(data).getBytes());
                    ProtocolUtils.writeVarInt(response, motdJson.readableBytes());
                    response.writeBytes(motdJson);
                    motdJson.release();

                    ctx.writeAndFlush(response);
                    System.out.println("Returned.");
                } else if (connectionStateManager.getClientState(address) == 2) {
                    System.out.println("登录尝试中...");
                    // 读取登录开始数据包
                    String username = ProtocolUtils.readString(msg);
                    UUID playerUUID = UUID.fromString("f6effa80-42ff-3435-a486-78afee95ee7b");

                    System.out.println("用户名: " + username);
                    System.out.println("UUID: " + playerUUID);

                    // 发送登录成功包
                    ByteBuf response1 = ctx.alloc().buffer();
                    ProtocolUtils.writeVarInt(response1, 0x02);
                    ctx.writeAndFlush(response1);
                    ByteBuf response2 = ctx.alloc().buffer();
                    ProtocolUtils.writeVarInt(response2, 0x03); // 登录成功包的ID
                    ctx.writeAndFlush(response2);

                    // 更新连接状态
                    connectionStateManager.setClientState(address, 3);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}