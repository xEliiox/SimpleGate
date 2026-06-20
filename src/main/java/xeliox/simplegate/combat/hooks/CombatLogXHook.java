package xeliox.simplegate.combat.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xeliox.simplegate.combat.CombatProvider;

import java.lang.reflect.Method;

public final class CombatLogXHook implements CombatProvider {

    private static final String PLUGIN_CLASS = "com.github.sirblobman.combatlogx.api.ICombatLogX";

    private final Object combatManager;
    private final Method isInCombatMethod;
    private final boolean available;

    public CombatLogXHook() {
        Object manager = null;
        Method method = null;
        boolean loaded = false;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (plugin != null) {
            try {
                Class<?> pluginClass = Class.forName(PLUGIN_CLASS);
                if (pluginClass.isInstance(plugin)) {
                    Method getCombatManager = pluginClass.getMethod("getCombatManager");
                    manager = getCombatManager.invoke(plugin);
                    method = manager.getClass().getMethod("isInCombat", Player.class);
                    loaded = true;
                }
            } catch (ReflectiveOperationException e) {
                Bukkit.getLogger().warning("[SimpleGate] Failed to hook CombatLogX: " + e.getMessage());
            }
        }

        this.combatManager = manager;
        this.isInCombatMethod = method;
        this.available = loaded;
    }

    @Override
    public String getName() {
        return "CombatLogX";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isInCombat(Player player) {
        if (!available) {
            return false;
        }
        try {
            return (boolean) isInCombatMethod.invoke(combatManager, player);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
