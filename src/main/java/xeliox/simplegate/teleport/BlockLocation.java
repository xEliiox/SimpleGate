package xeliox.simplegate.teleport;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockLocation {

    private int blockX = 0;
    private int blockY = 0;
    private int blockZ = 0;

    public BlockLocation() {
    }

    public BlockLocation(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    public static BlockLocation fromBlock(Block block) {
        return new BlockLocation(block.getX(), block.getY(), block.getZ());
    }

    public static BlockLocation fromLocation(Location location) {
        return new BlockLocation(
                (int) Math.floor(location.getX()),
                (int) Math.floor(location.getY()),
                (int) Math.floor(location.getZ())
        );
    }

    public Location getCenterLocation(String worldName) {
        return new Location(
                Bukkit.getWorld(worldName),
                blockX + 0.5,
                blockY + 0.5,
                blockZ + 0.5
        );
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(blockX)
                .append(blockY)
                .append(blockZ)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlockLocation other = (BlockLocation) obj;
        return new EqualsBuilder()
                .append(blockX, other.blockX)
                .append(blockY, other.blockY)
                .append(blockZ, other.blockZ)
                .isEquals();
    }

    @Override
    public String toString() {
        return "(" + blockX + ", " + blockY + ", " + blockZ + ")";
    }

    public int getBlockX() {
        return blockX;
    }

    public void setBlockX(int blockX) {
        this.blockX = blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public void setBlockY(int blockY) {
        this.blockY = blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public void setBlockZ(int blockZ) {
        this.blockZ = blockZ;
    }
}