package xyz.article.api.player;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import xyz.article.api.Location;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.world.World;

public class Player {
    private final GameProfile profile;
    private final Session session;

    private GameMode gameMode;
    private World world;
    private Inventory inventory;

    public Player(GameProfile profile, Session session, GameMode gameMode, Inventory inventory) {
        this.profile = profile;
        this.session = session;
        this.gameMode = gameMode;
        this.inventory = inventory;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public World getWorld() {
        return world;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void teleportTo(Location location) {
        this.world = location.world();
        session.send(new ClientboundPlayerPositionPacket(location.pos().getX(), location.pos().getY(), location.pos().getZ(), 0, 0, 0));
    }

    public Session getSession() {
        return session;
    }

    public void openInventory(Inventory inventory) {
        session.send(new ClientboundOpenScreenPacket(inventory.getContainerId(), inventory.getContainerType(), inventory.getName()));
        inventory.sync(session);
    }
}
