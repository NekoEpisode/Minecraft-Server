package xyz.article.api.world.block;

import java.util.HashMap;
import java.util.Map;

public class ItemToBlock {
    private static final Map<Integer, Integer> map = new HashMap<>();

    private static void writeMap() {
        map.put(0, 0); // air?
        map.put(1, 1); // stone
        map.put(27, 9); // grass_block
        map.put(2, 2); // granite
        map.put(3, 3); // polished_granite
        map.put(4, 4); // oh what's this, Diorite
    }

    public static int getBlockID(int itemID) {
        if (map.get(itemID) == null) {
            return 0;
        }
        return map.get(itemID);
    }
}
