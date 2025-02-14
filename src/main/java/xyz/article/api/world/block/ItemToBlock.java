package xyz.article.api.world.block;

import java.util.HashMap;
import java.util.Map;

public class ItemToBlock {
    private static final Map<Integer, Integer> map = new HashMap<>(); // 方块(物品)ID, 方块ID

    public static void writeMap() {
        map.put(0, 0); // Air?
        map.put(1, 1); // Stone
        map.put(2, 2); // Granite
        map.put(3, 3); // Polished Granite
        map.put(4, 4); // Oh what's that, Diorite
        map.put(5, 5); // Andesite
        map.put(7, 6); // Polished Andesite
        map.put(27, 9); // Grass Block
        map.put(28, 10); // Dirt
        map.put(29, 11); // Coarse Dirt
        map.put(30, 13); // Podzol
        map.put(35, 14); // Cobble Stone
        map.put(36, 15); // Oak Planks
        map.put(37, 16); // Spruce Planks
        map.put(38, 17); // Birch Planks
        map.put(39, 18); // Jungle Planks
        map.put(40, 19); // Acacia Planks
        map.put(41, 20); // Cherry Planks
        map.put(42, 21); // Dark Oak Planks
    }


    public static int getBlockID(int itemID) {
        System.out.println("对照表: 物品ID " + itemID);
        if (map.get(itemID) == null) {
            System.out.println("对照表: 未找到");
            return 0;
        }
        return map.get(itemID);
    }
}
