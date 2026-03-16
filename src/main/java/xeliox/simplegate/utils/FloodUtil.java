package xeliox.simplegate.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.gate.GateOrientation;

import java.util.HashSet;
import java.util.Set;

public class FloodUtil {

    /**
     * Given a void block, finds the connected void blocks as well as a frame of non-void blocks surrounding it.
     * If the given block is non-void, returns null.
     */
    public static FloodInfo getGateFloodInfo(Block clickedBlock, BlockFace clickedFace) {
        Block startBlock = clickedBlock.getRelative(clickedFace);

        if (!VoidUtil.isVoid(startBlock)) {
            return null;
        }

        int maxArea = SimpleGate.getInstance().getConfigManager().getMaxPortalSize();

        Set<Block> blocksNS = new FloodCalculator(GateOrientation.NS.getExpandFaces(), maxArea)
                .calcFloodBlocks(startBlock)
                .getFoundBlocks();
        Set<Block> blocksWE = new FloodCalculator(GateOrientation.WE.getExpandFaces(), maxArea)
                .calcFloodBlocks(startBlock)
                .getFoundBlocks();

        GateOrientation selectedOrientation;
        Set<Block> portalBlocks;

        if (blocksNS != null && blocksWE != null) {
            if (blocksNS.size() > blocksWE.size()) {
                selectedOrientation = GateOrientation.WE;
                portalBlocks = blocksWE;
            } else {
                selectedOrientation = GateOrientation.NS;
                portalBlocks = blocksNS;
            }
        } else if (blocksNS != null) {
            selectedOrientation = GateOrientation.NS;
            portalBlocks = blocksNS;
        } else if (blocksWE != null) {
            selectedOrientation = GateOrientation.WE;
            portalBlocks = blocksWE;
        } else {
            return null;
        }

        Set<Block> expandedBlocks = expandedByOne(portalBlocks, selectedOrientation.getExpandFaces());
        Set<Block> frameBlocks = SetUtils.diff(expandedBlocks, portalBlocks);

        return new FloodInfo(selectedOrientation,portalBlocks ,frameBlocks);
    }

    /**
     * Given a set of blocks, expands the set by adding all adjacent blocks reachable by one step in the given
     * directions.
     */
    private static Set<Block> expandedByOne(Set<Block> blocks, Set<BlockFace> expandFaces) {
        Set<Block> ret = new HashSet<>(blocks);
        for (Block block : blocks) {
            for (BlockFace face : expandFaces) {
                ret.add(block.getRelative(face));
            }
        }
        return ret;
    }

    public static class FloodInfo {
        private GateOrientation gateOrientation;
        private Set<Block> frameBlocks;
        private Set<Block> portalBlocks;

        public FloodInfo(GateOrientation gateOrientation, Set<Block> portalBlocks ,Set<Block> frameBlocks) {
            this.gateOrientation = gateOrientation;
            this.frameBlocks = frameBlocks;
            this.portalBlocks = portalBlocks;
        }

        public GateOrientation getGateOrientation() {
            return gateOrientation;
        }

        public Set<Block> getPortalBlocks() {
            return portalBlocks;
        }

        public Set<Block> getFrameBlocks() {
            return frameBlocks;
        }
    }

    /**
     * Given a block, recursively walks void adjacent blocks and returns the set with all of them, or null if the area
     * is too large.
     */
    private static class FloodCalculator {
        private final Set<BlockFace> expandFaces;
        private final int maxArea;
        private final Set<Block> foundBlocks = new HashSet<>();
        private boolean aborted = false;

        public FloodCalculator(Set<BlockFace> expandFaces, int maxArea) {
            this.expandFaces = expandFaces;
            this.maxArea = maxArea;
        }

        public Set<Block> getFoundBlocks() {
            return aborted || foundBlocks.isEmpty() ? null : foundBlocks;
        }

        public FloodCalculator calcFloodBlocks(Block startBlock) {
            if (foundBlocks.size() > maxArea) {
                aborted = true;
                return this;
            }

            if (foundBlocks.contains(startBlock)) {
                return this;
            }

            if (VoidUtil.isVoid(startBlock)) {
                foundBlocks.add(startBlock);
                for (BlockFace face : expandFaces) {
                    calcFloodBlocks(startBlock.getRelative(face));
                }
            }


            return this;
        }
    }
}