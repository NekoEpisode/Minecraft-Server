package xyz.article.chunk;

import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;

class ChunkData {
    private int chunkX;
    private int chunkZ;
    private byte[] chunkData;
    private LightUpdateData lightData;

    public ChunkData(int chunkX, int chunkZ, byte[] chunkData, LightUpdateData lightData) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunkData = chunkData;
        this.lightData = lightData;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    public LightUpdateData getLightData() {
        return lightData;
    }

    public void setBlock(int x, int y, int z, byte blockId) {
        int index = calculateIndex(x, y, z);
        if (index >= 0 && index < chunkData.length) {
            chunkData[index] = blockId;
        } else {
            throw new IndexOutOfBoundsException("Block position is out of chunk bounds");
        }
    }

    private int calculateIndex(int x, int y, int z) {
        return (y * 16 * 16) + (z * 16) + x;
    }
}