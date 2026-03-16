package xeliox.simplegate.listeners;

import api.xeliox.colorapi.ColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.config.ConfigManager;
import xeliox.simplegate.config.PortalType;
import xeliox.simplegate.gate.Gate;

import java.util.*;

public class PortalSelectorListener implements Listener {

    private final ConfigManager config;

    private final Map<Inventory, Gate> inventories = new HashMap<>();
    private final Map<Inventory, Runnable> finishActions = new HashMap<>();

    public PortalSelectorListener(SimpleGate plugin) {
        this.config = plugin.getConfigManager();
    }

    /* ================= API ================= */

    public void open(Player player, Gate gate, Runnable onFinish) {
        Inventory inventory = createInventory();

        fillInventory(inventory);

        inventories.put(inventory, gate);
        finishActions.put(inventory, onFinish);

        player.openInventory(inventory);
    }

    /* ================= Inventory ================= */

    private Inventory createInventory() {
        String title = ColorAPI.translate(config.getInventoryTitle());
        String type = config.getInventoryType().toUpperCase();

        switch (type) {

            case "CHEST":
                int size = config.getInventorySlots();

                if (size < 9) {
                    size = 9;
                } else if (size > 54) {
                    size = 54;
                }
                if (size % 9 != 0) {
                    size = (size / 9) * 9;
                }

                return Bukkit.createInventory(null, size, title);

            case "DISPENSER":
                return Bukkit.createInventory(null, InventoryType.DISPENSER, title);

            case "HOPPER":
            default:
                return Bukkit.createInventory(null, InventoryType.HOPPER, title);
        }
    }


    private void fillInventory(Inventory inventory) {
        if (config.isFillEmptySlots()) {
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(" ");
                filler.setItemMeta(meta);
            }
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        for (PortalType type : config.getPortalTypes().values()) {
            if (type.getSlot() < 0 || type.getSlot() >= inventory.getSize()) {
                continue;
            }

            ItemStack item = new ItemStack(type.getIconMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorAPI.translate(type.getName()));
                meta.setLore(ColorAPI.translate(type.getLore()));
                item.setItemMeta(meta);
            }

            inventory.setItem(type.getSlot(), item);
        }
    }

    /* ================= Events ================= */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventories.containsKey(inventory)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Gate gate = inventories.get(inventory);

        for (Map.Entry<Integer, PortalType> entry : config.getPortalTypes().entrySet()) {
            PortalType type = entry.getValue();
            if (type.getIconMaterial() == clicked.getType()) {
                gate.portalTypeId = entry.getKey();
                gate.setPortalType(type);
                gate.customFill(type.getContentMaterial());
                event.getWhoClicked().closeInventory();
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventories.containsKey(inventory)) return;

        Gate gate = inventories.remove(inventory);
        Runnable finish = finishActions.remove(inventory);

        if (gate != null && gate.isEmpty() && !gate.isFakeEndGateway()) {
            gate.customFill(Material.NETHER_PORTAL);
        }

        if (finish != null) {
            finish.run();
        }
    }
}
