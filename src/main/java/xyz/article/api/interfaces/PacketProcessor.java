package xyz.article.api.interfaces;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;

public interface PacketProcessor {
    void process(Packet packet, Session session);
}
