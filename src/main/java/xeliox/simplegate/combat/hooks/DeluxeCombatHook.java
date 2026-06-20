package xeliox.simplegate.combat.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xeliox.simplegate.combat.CombatProvider;

import java.lang.reflect.Method;

public final class DeluxeCombatHook implements CombatProvider {

    private static final String API_CLASS = "nl.marido.deluxecombat.api.DeluxeCombatAPI";

    private final Object api;
    private final Method isInCombatMethod;
    private final boolean available;

    public DeluxeCombatHook() {
        Object apiInstance = null;
        Method method = null;
        boolean loaded = false;

        if (Bukkit.getPluginManager().getPlugin("DeluxeCombat") != null) {
            try {
                Class<?> apiClass = Class.forName(API_CLASS);
                apiInstance = apiClass.getDeclaredConstructor().newInstance();
                method = apiClass.getMethod("isInCombat", Player.class);
                loaded = true;
            } catch (ReflectiveOperationException e) {
                Bukkit.getLogger().warning("[SimpleGate] Failed to hook DeluxeCombat: " + e.getMessage());
            }
        }

        this.api = apiInstance;
        this.isInCombatMethod = method;
        this.available = loaded;
    }

    @Override
    public String getName() {
        return "DeluxeCombat";
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
            return (boolean) isInCombatMethod.invoke(api, player);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
