package xeliox.simplegate.combat;

import org.bukkit.entity.Player;

public interface CombatProvider {

    String getName();

    boolean isAvailable();

    boolean isInCombat(Player player);
}
