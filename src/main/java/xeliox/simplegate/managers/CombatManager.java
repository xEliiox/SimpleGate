package xeliox.simplegate.managers;

import org.bukkit.entity.Player;
import xeliox.simplegate.combat.CombatProvider;
import xeliox.simplegate.combat.InternalCombatTagger;
import xeliox.simplegate.combat.hooks.CombatLogXHook;
import xeliox.simplegate.combat.hooks.DeluxeCombatHook;
import xeliox.simplegate.combat.hooks.PvPManagerHook;
import xeliox.simplegate.config.Messages;
import xeliox.simplegate.config.Permissions;
import xeliox.simplegate.gate.Gate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class CombatManager {

    private final InternalCombatTagger internalTagger = new InternalCombatTagger();
    private final List<CombatProvider> externalProviders = new ArrayList<>();

    private boolean blockTeleportInCombat;
    private int internalTagSeconds;

    public void load(boolean blockTeleportInCombat,
                     int internalTagSeconds,
                     boolean hookDeluxeCombat,
                     boolean hookCombatLogX,
                     boolean hookPvPManager,
                     Logger logger) {
        this.blockTeleportInCombat = blockTeleportInCombat;
        this.internalTagSeconds = internalTagSeconds;

        internalTagger.setTagDurationSeconds(internalTagSeconds);
        reloadExternalProviders(hookDeluxeCombat, hookCombatLogX, hookPvPManager, logger);
    }

    private void reloadExternalProviders(boolean hookDeluxeCombat,
                                         boolean hookCombatLogX,
                                         boolean hookPvPManager,
                                         Logger logger) {
        externalProviders.clear();

        if (hookDeluxeCombat) {
            registerProvider(new DeluxeCombatHook(), logger);
        }
        if (hookCombatLogX) {
            registerProvider(new CombatLogXHook(), logger);
        }
        if (hookPvPManager) {
            registerProvider(new PvPManagerHook(), logger);
        }

        if (externalProviders.isEmpty()) {
            if (blockTeleportInCombat && internalTagSeconds > 0) {
                logger.info("[SimpleGate] No combat plugin detected. Using internal combat tag (" + internalTagSeconds + "s).");
            }
        } else {
            logger.info("[SimpleGate] Combat hooks active: " + describeActiveProviders());
        }
    }

    private void registerProvider(CombatProvider provider, Logger logger) {
        if (provider.isAvailable()) {
            externalProviders.add(provider);
            logger.info("[SimpleGate] Hooked combat provider: " + provider.getName());
        }
    }

    private String describeActiveProviders() {
        StringBuilder builder = new StringBuilder();
        for (CombatProvider provider : externalProviders) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(provider.getName());
        }
        return builder.toString();
    }

    public InternalCombatTagger getInternalTagger() {
        return internalTagger;
    }


    public boolean isInCombat(Player player) {
        if (!blockTeleportInCombat || Permissions.COMBAT_BYPASS.has(player)) {
            return false;
        }

        for (CombatProvider provider : externalProviders) {
            if (provider.isInCombat(player)) {
                return true;
            }
        }

        if (externalProviders.isEmpty() && internalTagSeconds > 0) {
            return internalTagger.isTagged(player);
        }

        return false;
    }

    public boolean blockPortalUse(Player player, Gate gate) {
        if (!isInCombat(player)) {
            return false;
        }

        player.sendMessage(Messages.PREFIX.getMessage() + Messages.COMBAT_BLOCKED.getMessage());
        gate.playerKb(player, 0.6, 0.4);
        return true;
    }
}
