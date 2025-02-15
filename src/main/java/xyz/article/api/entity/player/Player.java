package xyz.article.api.entity.player;

import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector2d;
import org.cloudburstmc.math.vector.Vector2f;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import xyz.article.api.Location;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.world.World;

public class Player {
    private final GameProfile profile;
    private final Session session;
    private final int entityID;

    private GameMode gameMode;
    private World world;
    private Inventory inventory;

    private final Hand mainHand;
    private final Hand leftHand;
    private ItemStack draggingItem;
    private Location location;
    private Vector2f angle;

    public Player(GameProfile profile, Session session, int entityID, GameMode gameMode, Inventory inventory, World world, Location location, Vector2f angle) {
        this.profile = profile;
        this.session = session;
        this.entityID = entityID;
        this.gameMode = gameMode;
        this.inventory = inventory;
        this.world = world;
        this.location = location;
        this.angle = angle;

        this.mainHand = new Hand(0); // 0 == 右手
        this.leftHand = new Hand(1); // 1 == 左手
    }

    public GameProfile getProfile() {
        return profile;
    }

    public int getEntityID() {
        return entityID;
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

    public Hand getLeftHand() {
        return leftHand;
    }

    public Hand getMainHand() {
        return mainHand;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Vector2f getAngle() {
        return angle;
    }

    public void setAngle(Vector2f angle) {
        this.angle = angle;
    }

    public Session getSession() {
        return session;
    }

    public void teleportTo(Location location) {
        this.world = location.world();
        session.send(new ClientboundPlayerPositionPacket(location.pos().getX(), location.pos().getY(), location.pos().getZ(), 0, 0, 0));
    }

    public void openInventory(Inventory inventory) {
        session.send(new ClientboundOpenScreenPacket(inventory.getContainerId(), inventory.getContainerType(), inventory.getName()));
        session.send(new ClientboundContainerSetContentPacket(inventory.getContainerId(), 0, inventory.getItems(), inventory.getItem(46)));
    }

    public void sendMessage(Component message) {
        session.send(new ClientboundSystemChatPacket(message, false));
    }
}
