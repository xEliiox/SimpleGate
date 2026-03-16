package xeliox.simplegate.utils;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryUtil {

    private static final int SIZE_PLAYER_STORAGE = 36;
    private static final int SIZE_PLAYER_ARMOR = 4;

    // 0 --> 36 (35 exclusive)
    private static final int INDEX_PLAYER_STORAGE_FROM = 0;
    private static final int INDEX_PLAYER_STORAGE_TO = INDEX_PLAYER_STORAGE_FROM + SIZE_PLAYER_STORAGE;

    // 36 --> 40 (39 exclusive)
    private static final int INDEX_PLAYER_ARMOR_FROM = INDEX_PLAYER_STORAGE_TO;
    private static final int INDEX_PLAYER_ARMOR_TO = INDEX_PLAYER_ARMOR_FROM + SIZE_PLAYER_ARMOR;

    // 40 --> 41 (40 exclusive)
    private static final int INDEX_PLAYER_EXTRA_FROM = INDEX_PLAYER_ARMOR_TO;

    // 40
    private static final int INDEX_PLAYER_SHIELD = INDEX_PLAYER_EXTRA_FROM;

    private static PlayerInventory asPlayerInventory(Inventory inventory) {
        return inventory instanceof PlayerInventory ? (PlayerInventory) inventory : null;
    }

    public static ItemStack getWeapon(HumanEntity human) {
        ItemStack ret = human.getInventory().getItemInMainHand();
        return ret.getType() == Material.AIR ? null : ret;
    }

    public static void setWeapon(HumanEntity human, ItemStack weapon) {
        if (human == null) return;
        human.getInventory().setItemInMainHand(weapon);
    }

    public static ItemStack getShield(PlayerInventory playerInventory) {
        ItemStack[] contents = playerInventory.getContents();
        if (contents.length <= INDEX_PLAYER_SHIELD) return null;
        ItemStack ret = contents[INDEX_PLAYER_SHIELD];
        return ret != null && ret.getType() != Material.AIR ? ret : null;
    }

    public static void setShield(Inventory inventory, ItemStack shield) {
        PlayerInventory playerInventory = asPlayerInventory(inventory);
        if (playerInventory == null) return;
        ItemStack[] contents = playerInventory.getContents();
        if (contents.length <= INDEX_PLAYER_SHIELD) return;
        inventory.setItem(INDEX_PLAYER_SHIELD, shield);
    }

    public static ItemStack getShield(HumanEntity human) {
        return getShield(human.getInventory());
    }

    public static void setShield(HumanEntity human, ItemStack shield) {
        if (human == null) return;
        setShield(human.getInventory(), shield);
    }

    public static void updateSoon(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.updateInventory();
            }
        }.run();
    }
}