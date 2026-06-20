package xeliox.simplegate.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InternalCombatTagger implements Listener {

    private final Map<UUID, Long> combatUntil = new HashMap<>();
    private long tagDurationMs;

    public void setTagDurationSeconds(int seconds) {
        this.tagDurationMs = Math.max(0, seconds) * 1000L;
    }

    public boolean isTagged(Player player) {
        if (tagDurationMs <= 0) {
            return false;
        }
        Long until = combatUntil.get(player.getUniqueId());
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            combatUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void clear(Player player) {
        combatUntil.remove(player.getUniqueId());
    }

    public void clearAll() {
        combatUntil.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (tagDurationMs <= 0) {
            return;
        }

        tag(resolvePlayer(event.getEntity()));
        tag(resolvePlayer(event.getDamager()));
    }

    private Player resolvePlayer(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        }
        if (entity instanceof org.bukkit.entity.Projectile) {
            Object shooter = ((org.bukkit.entity.Projectile) entity).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }

    private void tag(Player player) {
        if (player == null) {
            return;
        }
        combatUntil.put(player.getUniqueId(), System.currentTimeMillis() + tagDurationMs);
    }
}
