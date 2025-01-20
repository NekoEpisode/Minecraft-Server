package xyz.article.protocol.packets;

import io.netty.buffer.ByteBuf;

public abstract class Packet {
    public abstract void encode(ByteBuf buf);
    public abstract void decode(ByteBuf buf);
}