package xyz.article.chunk;

import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;

import java.util.BitSet;
import java.util.List;

public class Chunk {
    public static ChunkData createSimpleGrassChunk() {
        DataPalette palette = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(PaletteType.CHUNK.getMaxBitsPerEntry(), 16 * 16 * 16), PaletteType.CHUNK);

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    palette.set(x, y, z, 9);
                }
            }
        }

        return new ChunkData(0, 0, palette, new HeightMap(), new BlockEntityInfo[]{}, new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of()));
    }
}
