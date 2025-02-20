package xyz.article.api.entity.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.Vector2f;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import xyz.article.api.Location;
import xyz.article.api.command.CommandSender;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.inventory.PlayerInventory;
import xyz.article.api.world.World;
import org.geysermc.mcprotocollib.network.packet.Packet;

import java.util.UUID;

public class Player extends CommandSender {
    private final GameProfile profile;
    private final Session session;
    private final int entityID;

    private GameMode gameMode;
    private World world;
    private PlayerInventory inventory;

    private final Hand mainHand;
    private final Hand leftHand;
    private Location location;
    private Vector2f angle;

    public Player(GameProfile profile, Session session, int entityID, GameMode gameMode, PlayerInventory inventory, World world, Location location, Vector2f angle) {
        this.profile = profile;
        this.session = session;
        this.entityID = entityID;
        this.gameMode = gameMode;
        this.inventory = inventory;
        this.world = world;
        world.addSession(session);
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

    public PlayerInventory getInventory() {
        return inventory;
    }

    public void setInventory(PlayerInventory inventory) {
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

    @Override
    public String getName() {
        return profile.getName();
    }

    @Override
    public UUID getUuid() {
        return profile.getId();
    }

    public void teleportTo(Location location) {
        setWorld(location.world());
        session.send(new ClientboundPlayerPositionPacket(location.pos().getX(), location.pos().getY(), location.pos().getZ(), 0, 0, 0));
    }

    public void setWorld(World world) {
        this.world.removeSession(session);
        this.world = world;
        world.addSession(session);
    }

    public void openInventory(Inventory inventory) {
        session.send(new ClientboundOpenScreenPacket(inventory.getContainerId(), inventory.getContainerType(), inventory.getName()));
        session.send(new ClientboundContainerSetContentPacket(inventory.getContainerId(), 0, inventory.getItems(), inventory.getItem(46)));
    }

    @Override
    public void sendMessage(String message) {
        session.send(new ClientboundSystemChatPacket(Component.text(message), false));
    }
    public void sendMessage(Component component) {
        session.send(new ClientboundSystemChatPacket(component, false));
    }
    
    public void sendPacket(Packet packet){
        session.send(packet);
    }

    public void throwItem(int slot) {
        if (slot < 0 || slot >= 46) {
            throw new IllegalArgumentException("Slot out of bounds (0-46 but received " + slot + ")");
        }
    }
}
