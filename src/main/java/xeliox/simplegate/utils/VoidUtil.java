package xeliox.simplegate.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;


public class VoidUtil {

    public static boolean isVoid(Material material) {
        return isMaterialAir(material) || !material.isSolid();
    }


    public static boolean isVoid(Block block) {
        return isVoid(block.getType());
    }

    public static boolean isMaterialAir(Material material) {
        return material == Material.AIR ||
                material.name().equals("VOID_AIR") ||
                material.name().equals("CAVE_AIR");
    }
}