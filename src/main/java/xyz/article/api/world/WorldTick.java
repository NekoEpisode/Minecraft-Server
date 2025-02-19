package xyz.article.api.world;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundTeleportEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import xyz.article.api.Location;
import xyz.article.api.entity.Entity;
import xyz.article.api.entity.ItemEntity;
import xyz.article.api.entity.player.Player;
import xyz.article.api.inventory.PlayerInventory;

import java.util.*;

import static xyz.article.RunningData.playerList;

/**
 * WorldTick类用于定义世界的Tick逻辑
 */
public class WorldTick {
    private final World world;
    private long worldAge = 0;
    // 用于记录 tick 时间
    private final Queue<Long> tickTimes = new LinkedList<>();
    private final Map<Entity, Location> cache = new HashMap<>();

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

        for (Entity entity : world.getEntities()) {
            for (Player player : playerList) {
                if (cache.get(entity) != null && !cache.get(entity).equals(entity.getLocation())) {
                    cache.put(entity, entity.getLocation());
                    player.sendPacket(new ClientboundTeleportEntityPacket(entity.getEntityId(), entity.getLocation().pos().getX(), entity.getLocation().pos().getY(), entity.getLocation().pos().getZ(), 0, 0, true));
                }
            }

            // FIXME: 这里的物品捡起了两次
            if (entity instanceof ItemEntity itemEntity) {
                long currentTime1 = System.currentTimeMillis();
                if (currentTime1 - itemEntity.getSpawnTime() > 2500) {
                    Location entityLocation = entity.getLocation();
                    double closestPlayerDistance = Double.MAX_VALUE;
                    Player closestPlayer = null;
                    for (Player player : playerList) {
                        double distance = entityLocation.distance(player.getLocation());
                        if (distance < closestPlayerDistance) {
                            closestPlayerDistance = distance;
                            closestPlayer = player;
                        }
                    }

                    if (closestPlayer != null) {
                        int slot = -1;
                        PlayerInventory inventory = closestPlayer.getInventory();
                        for (int i = 9; i < inventory.getSize() - 2; i++) {
                            ItemStack itemStack = inventory.getItem(i);
                            if (itemStack != null) {
                                if (itemStack.getAmount() < 64 && itemStack.equals(itemEntity.getItemStack())) {
                                    slot = i;
                                    break;
                                }
                            }
                            if (itemStack == null) {
                                slot = i;
                                break;
                            }
                        }

                        if (slot != -1) {
                            final double PICKUP_RANGE = 1.5;
                            if (closestPlayerDistance < PICKUP_RANGE) {
                                ItemStack itemStack;
                                if (inventory.getItem(slot) != null) {
                                    ItemStack itemStack1 = inventory.getItem(slot);
                                    itemStack = new ItemStack(itemStack1.getId(), itemStack1.getAmount() + 1, itemStack1.getDataComponents());
                                }else {
                                    itemStack = itemEntity.getItemStack();
                                }
                                if (world.getEntities().contains(entity)) {
                                    world.getEntities().remove(entity);
                                    for (Player player : playerList) {
                                        player.sendPacket(new ClientboundRemoveEntitiesPacket(new int[]{entity.getEntityId()}));
                                    }
                                    System.out.println("捡起, slot" + slot);
                                    inventory.setItem(slot, itemStack);
                                    closestPlayer.sendPacket(new ClientboundContainerSetContentPacket(0, 0, inventory.getItems(), inventory.getDragging()));
                                }
                            }
                        }
                    }
                }
            }
        }
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
