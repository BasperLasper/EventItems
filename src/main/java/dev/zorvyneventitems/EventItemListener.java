package dev.zorvyneventitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EventItemListener implements Listener {

    private final ZorvynEventItems plugin;

    public EventItemListener(ZorvynEventItems plugin) { this.plugin = plugin; }

    /** When a player joins, strip any items from ended kits they might have */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        scanAndStrip(event.getPlayer());
    }

    /** When a player clicks in any inventory, check the clicked item */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        String kitId = ItemUtil.getKitId(item);
        if (kitId == null) return;

        // If the kit no longer exists (ended), remove the item immediately
        if (!plugin.getActiveKits().containsKey(kitId)) {
            event.setCancelled(true);
            event.getInventory().setItem(event.getSlot(), null);
            player.sendMessage(Component.text("✦ That event item has expired and was removed.", NamedTextColor.RED));
        }
    }

    /** When a player opens an inventory (chest, ender chest etc), scan for expired items */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        // Run next tick so the inventory is fully loaded
        plugin.getServer().getScheduler().runTask(plugin, () -> scanAndStrip(player));
    }

    /** Strip all items belonging to no-longer-active kits from a player */
    public void scanAndStrip(Player player) {
        stripInventory(player, player.getInventory().getContents(), false);
        stripInventory(player, player.getEnderChest().getContents(), true);
    }

    private void stripInventory(Player player, ItemStack[] contents, boolean isEnder) {
        boolean removed = false;
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            String kitId = ItemUtil.getKitId(item);
            if (kitId != null && !plugin.getActiveKits().containsKey(kitId)) {
                if (isEnder) player.getEnderChest().setItem(i, null);
                else player.getInventory().setItem(i, null);
                removed = true;
            }
        }
        if (removed) {
            player.sendMessage(Component.text("✦ Some expired event items were removed from your inventory.", NamedTextColor.RED));
        }
    }
}
