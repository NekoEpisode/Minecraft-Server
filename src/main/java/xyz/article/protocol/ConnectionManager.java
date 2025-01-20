package xyz.article.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {

    private final Map<SocketAddress, Integer> clientStates = new HashMap<>();

    public void setClientState(SocketAddress address, int state) {
        clientStates.put(address, state);
    }

    public int getClientState(SocketAddress address) {
        return clientStates.getOrDefault(address, 0); // 默认状态为 0
    }

    public int getNextState(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        SocketAddress address = ctx.channel().remoteAddress();
        int currentState = getClientState(address);

        if (currentState == 0) { // Expecting Handshake
            if (msg.isReadable()) {
                int packetId = ProtocolUtils.readVarInt(msg);
                if (packetId == 0x00) { // Handshake packet
                    // Read handshake packet content
                    ProtocolUtils.readVarInt(msg); // protocolVersion
                    ProtocolUtils.readString(msg); // serverAddress
                    msg.readUnsignedShort(); // serverPort
                    int nextState = ProtocolUtils.readVarInt(msg);
                    setClientState(address, nextState);
                    return nextState;
                }
            }
        } else {
            System.err.println("Unexpected client state: " + currentState);
        }
        return currentState;
    }
}
