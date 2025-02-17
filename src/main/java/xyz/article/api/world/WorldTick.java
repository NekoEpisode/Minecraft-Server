package xyz.article.api.world;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityMotionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import org.jetbrains.annotations.NotNull;
import xyz.article.api.Location;
import xyz.article.api.entity.Entity;
import xyz.article.api.entity.ItemEntity;
import xyz.article.api.entity.player.Player;
import xyz.article.api.inventory.Inventory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static xyz.article.RunningData.playerList;

/**
 * WorldTick类用于定义世界的Tick逻辑
 */
public class WorldTick {
    private final World world;
    private long worldAge = 0;
    // 用于记录 tick 时间
    private final Queue<Long> tickTimes = new LinkedList<>();

    public WorldTick(World world) {
        this.world = world;
    }

    protected void tick() {
        long currentTime = System.currentTimeMillis();

        // 记录当前 tick 的时间
        tickTimes.add(currentTime);

        // 清理超过 15 分钟的记录
        while (!tickTimes.isEmpty() && currentTime - tickTimes.peek() > 15 * 60 * 1000) {
            tickTimes.poll();
        }

        //计算时间
        // 每次 tick 增加 1 个刻度
        world.worldTime = world.worldTime + 2;

        for (Session session : world.getSessions()) {
            session.send(new ClientboundSetTimePacket(worldAge, world.worldTime));
        }

        // 如果时间超过一天（24000 刻度），则重置为 0
        if (world.worldTime >= 24000) {
            world.worldTime = 0;
            worldAge++;
        }

        /*for (Session session : sessions) {
            Player player = Slider.getPlayer(session);
            List<int[]> list; //获取圆内各个区块坐标
            if (player != null) {
                list = MathUtils.getChunkCoordinatesInCircle(((int) player.getLocation().pos().getX() >> 4), ((int) player.getLocation().pos().getZ() >> 4), 4);
                for (int[] coord : list) {
                    if (chunkDataMap.get(Vector2i.from(coord[0], coord[1])) == null) { //检查玩家所处的世界内是否已经有这个区块
                        ChunkData chunkData = Chunk.createSimpleGrassChunk(coord[0], coord[1]);
                        chunkDataMap.put(Vector2i.from(coord[0], coord[1]), chunkData);
                        System.out.println("new");
                        for (Session session1 : sessions) {
                            session1.send(chunkData.getChunkPacket());
                        }
                    } else {
                        for (Session session1 : sessions) {
                            session1.send(chunkDataMap.get(Vector2i.from(coord[0], coord[1])).getChunkPacket());
                        }
                    }
                }
            }
        }*/


        //未完成
        /*for (Entity entity : world.getEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                Player player = getClosestPlayerNearby(entity, playerList);
                if (player != null) {
                    int slot = -1;
                    Inventory inventory = player.getInventory();
                    for (int i = 9; i < inventory.getSize() - 2; i++) {
                        if (inventory.getItem(i) == null) {
                            slot = i;
                            break;
                        }
                    }

                    if (slot != -1) {
                        Location entityLocation = entity.getLocation();
                        Location playerLocation = player.getLocation();

                        // 计算从实体到玩家的向量
                        ClientboundSetEntityMotionPacket motionPacket = getClientboundSetEntityMotionPacket(entity, playerLocation, entityLocation);
                        player.sendPacket(motionPacket);

                        if (entityLocation.distance(playerLocation) < 0.3) {
                            player.sendPacket(new ClientboundRemoveEntitiesPacket(new int[]{entity.getEntityId()}));
                            inventory.setItem(slot, itemEntity.getItemStack());
                            player.sendPacket(new ClientboundContainerSetContentPacket(-1, 0, inventory.getItems(), inventory.getItem(36)));
                        }
                    }
                }
            }
        }*/
    }

    private @NotNull ClientboundSetEntityMotionPacket getClientboundSetEntityMotionPacket(Entity entity, Location playerLocation, Location entityLocation) {
        double vectorX = playerLocation.pos().getX() - entityLocation.pos().getX();
        double vectorY = playerLocation.pos().getY() - entityLocation.pos().getY();
        double vectorZ = playerLocation.pos().getZ() - entityLocation.pos().getZ();

        double length = Math.sqrt(vectorX * vectorX + vectorY * vectorY + vectorZ * vectorZ);
        vectorX /= length;
        vectorY /= length;
        vectorZ /= length;

        double motionX = vectorX * 0.5;
        double motionY = vectorY * 0.5;
        double motionZ = vectorZ * 0.5;

        return new ClientboundSetEntityMotionPacket(entity.getEntityId(), motionX, motionY, motionZ);
    }

    protected Player getClosestPlayerNearby(Entity entity, List<Player> players) {
        double entityX = entity.getLocation().pos().getX();
        double entityY = entity.getLocation().pos().getY();
        double entityZ = entity.getLocation().pos().getZ();
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        // 遍历实体周围的每个格子
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    // 计算格子中心点的坐标
                    double centerX = entityX + dx + 0.5;
                    double centerY = entityY + dy + 0.5;
                    double centerZ = entityZ + dz + 0.5;

                    // 检查是否有玩家在这个格子里
                    for (Player player : players) {
                        double distance = isPlayerInBlock(player, centerX, centerY, centerZ);
                        if (distance != -1 && distance < closestDistance) {
                            closestDistance = distance;
                            closestPlayer = player;
                        }
                    }
                }
            }
        }
        return closestPlayer;
    }

    private double isPlayerInBlock(Player player, double centerX, double centerY, double centerZ) {
        double playerX = player.getLocation().pos().getX();
        double playerY = player.getLocation().pos().getY();
        double playerZ = player.getLocation().pos().getZ();

        // 计算玩家与格子中心点的距离
        double distance = Math.sqrt(
                Math.pow(playerX - centerX, 2) +
                        Math.pow(playerY - centerY, 2) +
                        Math.pow(playerZ - centerZ, 2)
        );

        // 如果玩家在格子内，返回距离，否则返回-1
        return distance <= 0.5 ? distance : -1;
    }


    /**
     * 计算指定时间范围内的 TPS
     *
     * @param timeRangeInSeconds 时间范围（秒）
     * @return TPS
     */
    private double calculateTPS(int timeRangeInSeconds) {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - timeRangeInSeconds * 1000L;

        // 统计在时间范围内的 tick 次数
        int tickCount = 0;
        for (long tickTime : tickTimes) {
            if (tickTime >= startTime) {
                tickCount++;
            }
        }

        return (double) tickCount / timeRangeInSeconds;
    }

    /**
     * 获取TPS
     *
     * @return index[0] 1s, [1] 5s, [2] 15s, [3] 1m, [4] 5m, [5] 15m
     */
    public List<Double> getTPS() {
        List<Double> doubles = new ArrayList<>();
        doubles.add(Math.round(calculateTPS(1) * 100.0) / 100.0);
        doubles.add(Math.round(calculateTPS(5) * 100.0) / 100.0);
        doubles.add(Math.round(calculateTPS(15) * 100.0) / 100.0);
        doubles.add(Math.round(calculateTPS(60) * 100.0) / 100.0);
        doubles.add(Math.round(calculateTPS(5 * 60) * 100.0) / 100.0);
        doubles.add(Math.round(calculateTPS(15 * 60) * 100.0) / 100.0);
        return doubles;
    }
}
