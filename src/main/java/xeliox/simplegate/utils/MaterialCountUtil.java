package xeliox.simplegate.utils;

import io.github.xeliiox.colorapi.ColorAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialCountUtil {

    public static Map<Material, Long> count(Collection<Block> blocks) {
        return blocks.stream()
                .collect(Collectors.groupingBy(
                        Block::getType,
                        () -> new EnumMap<>(Material.class),
                        Collectors.counting()
                ));
    }

    public static boolean has(Map<Material, Long> me, Map<Material, Long> req) {
        if (me == null || req == null) return false;

        return req.entrySet().stream()
                .allMatch(e -> me.getOrDefault(e.getKey(), 0L) >= e.getValue());
    }


    public static String desc(Map<Material, Long> materialCounts) {
        List<String> parts = materialCounts.entrySet().stream()
                .map(e -> ColorAPI.translate(
                        "&d" + e.getValue() + " &b" + Util.getMaterialName(e.getKey())
                ))
                .collect(Collectors.toList());

        return Util.implodeCommaAnd(
                parts,
                ColorAPI.translate("&7, "),
                ColorAPI.translate(" &7y ")
        );
    }

}
