package xeliox.simplegate.utils;

import org.bukkit.entity.Player;

public final class BoundingBoxFactory {

    public static IBoundingBox of(Player player) {
        return VersionCheck.serverIsLegacy()
                ? new PlayerBoundingBoxLegacy(player)
                : new PlayerBoundingBoxModern(player);
    }

    public static IBoundingBox of(org.bukkit.block.Block block) {
        return new BlockBoundingBox(block);
    }
}
