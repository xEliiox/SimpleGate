package xeliox.simplegate.gate.dto;

public class BlockLocationDTO {
    private int blockX;
    private int blockY;
    private int blockZ;

    public BlockLocationDTO() {
    }

    public BlockLocationDTO(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    public int getBlockX() { return blockX; }
    public int getBlockY() { return blockY; }
    public int getBlockZ() { return blockZ; }
}
