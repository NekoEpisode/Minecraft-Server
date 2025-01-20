package xyz.article.protocol.packets.handshake;

import io.netty.buffer.ByteBuf;
import xyz.article.protocol.ProtocolUtils;
import xyz.article.protocol.packets.Packet;

public class HandshakePacket extends Packet {
    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int nextState;

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getNextState() {
        return nextState;
    }

    @Override
    public void encode(ByteBuf buf) {
        ProtocolUtils.writeVarInt(buf, protocolVersion);
        ProtocolUtils.writeString(buf, serverAddress);
        buf.writeShort(serverPort);
        ProtocolUtils.writeVarInt(buf, nextState);
    }

    @Override
    public void decode(ByteBuf buf) {
        protocolVersion = ProtocolUtils.readVarInt(buf);
        serverAddress = ProtocolUtils.readString(buf);
        serverPort = buf.readUnsignedShort();
        nextState = ProtocolUtils.readVarInt(buf);
    }
}