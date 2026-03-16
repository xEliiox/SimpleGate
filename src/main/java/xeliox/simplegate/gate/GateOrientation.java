package xeliox.simplegate.gate;

import org.bukkit.block.BlockFace;
import xeliox.simplegate.teleport.BlockLocation;
import xeliox.simplegate.utils.YawUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum GateOrientation {

    NS(new LinkedHashSet<>(Arrays.asList(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.UP,
            BlockFace.DOWN
    ))),

    WE(new LinkedHashSet<>(Arrays.asList(
            BlockFace.WEST,
            BlockFace.EAST,
            BlockFace.UP,
            BlockFace.DOWN
    )));

    private final Set<BlockFace> expandFaces;

    GateOrientation(Set<BlockFace> expandFaces) {
        this.expandFaces = Collections.unmodifiableSet(new LinkedHashSet<>(expandFaces));
    }

    public Set<BlockFace> getExpandFaces() {
        return expandFaces;
    }

    public BlockFace getExitFace(BlockLocation exitBlockCoords, BlockLocation gateBlockCoords) {
        int mod;
        if (this == NS) {
            mod = exitBlockCoords.getBlockX() - gateBlockCoords.getBlockX();
            return mod > 0 ? BlockFace.WEST : BlockFace.EAST;
        } else {
            mod = exitBlockCoords.getBlockZ() - gateBlockCoords.getBlockZ();
            return mod > 0 ? BlockFace.NORTH : BlockFace.SOUTH;
        }
    }

    public float getExitYaw(BlockLocation exit, BlockLocation gate) {
        Float yaw = YawUtil.getYaw(getExitFace(exit, gate));
        return yaw != null ? yaw : 0f;
    }
}