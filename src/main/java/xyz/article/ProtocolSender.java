package xyz.article;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;

import java.nio.charset.StandardCharsets;

public class ProtocolSender {
    public static void sendBrand(String brandName, Session session) {
        ByteBuf buf = Unpooled.buffer(brandName.length() + 1);
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        helper.writeVarInt(buf, brandName.length());
        buf.writeBytes(brandName.getBytes(StandardCharsets.UTF_8));
        session.send(new ClientboundCustomPayloadPacket(Key.key("minecraft:brand"), buf.array()));
    }
}
