package xeliox.simplegate.combat.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xeliox.simplegate.combat.CombatProvider;

import java.lang.reflect.Method;

public final class PvPManagerHook implements CombatProvider {

    private static final String PLUGIN_CLASS = "me.NoChance.PvPManager.PvPManager";

    private final Object pvpManager;
    private final Method isInCombatMethod;
    private final boolean available;

    public PvPManagerHook() {
        Object manager = null;
        Method method = null;
        boolean loaded = false;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("PvPManager");
        if (plugin != null) {
            try {
                Class<?> pluginClass = Class.forName(PLUGIN_CLASS);
                if (pluginClass.isInstance(plugin)) {
                    manager = plugin;
                    method = pluginClass.getMethod("isInCombat", Player.class);
                    loaded = true;
                }
            } catch (ReflectiveOperationException e) {
                Bukkit.getLogger().warning("[SimpleGate] Failed to hook PvPManager: " + e.getMessage());
            }
        }

        this.pvpManager = manager;
        this.isInCombatMethod = method;
        this.available = loaded;
    }

    @Override
    public String getName() {
        return "PvPManager";
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
            return (boolean) isInCombatMethod.invoke(pvpManager, player);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
