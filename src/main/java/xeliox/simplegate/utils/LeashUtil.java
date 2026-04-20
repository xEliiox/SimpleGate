package xeliox.simplegate.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class LeashUtil {

    private static final int SEARCH_RADIUS = 20;

    public static Set<LivingEntity> getLeashedEntities(Entity start) {
        Set<LivingEntity> result = new HashSet<>();
        Set<Entity> visited = new HashSet<>();
        Queue<Entity> toProcess = new ArrayDeque<>();

        toProcess.add(start);
        visited.add(start);

        while (!toProcess.isEmpty()) {
            Entity current = toProcess.poll();

            if (current instanceof LivingEntity && !(current instanceof Player)) {
                result.add((LivingEntity) current);
            }

            for (Entity nearby : current.getWorld().getNearbyEntities(
                    current.getLocation(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)) {

                if (!(nearby instanceof LivingEntity)) continue;
                if (nearby instanceof Player) continue;
                LivingEntity living = (LivingEntity) nearby;
                if (!living.isLeashed()) continue;
                if (visited.contains(living)) continue;
                if (living.getLeashHolder().equals(current)) {
                    visited.add(living);
                    toProcess.add(living);
                }
            }

            if (current instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) current;
                if (living.isLeashed()) {
                    Entity holder = living.getLeashHolder();
                    if (!(holder instanceof Player) && !visited.contains(holder)) {
                        visited.add(holder);
                        toProcess.add(holder);
                        if (holder instanceof LivingEntity) {
                            result.add((LivingEntity) holder);
                        }
                    }
                }
            }
        }
        return result;
    }
}