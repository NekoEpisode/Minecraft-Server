package xyz.article.api.player;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import xyz.article.api.Location;
import xyz.article.api.world.World;

public class Player {
    private final GameProfile profile;
    private final Session session;

    private World inWorld;

    public Player(GameProfile profile, Session session) {
        this.profile = profile;
        this.session = session;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public void teleportTo(Location location) {
        this.inWorld = location.getWorld();
        session.send(new ClientboundPlayerPositionPacket(location.getPos().getX(), location.getPos().getY(), location.getPos().getZ(), 0, 0, 0));
    }

    public World getWorld() {
        return inWorld;
    }
}
