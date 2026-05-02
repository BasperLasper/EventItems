package dev.zorvyneventitems;

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        scanAndStrip(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        String kitId = ItemUtil.getKitId(item);
        if (kitId == null) return;
        if (!plugin.getActiveKits().containsKey(kitId)) {
            event.setCancelled(true);
            event.getInventory().setItem(event.getSlot(), null);
            player.sendMessage(plugin.getMessages().get("expired-item-removed"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> scanAndStrip(player));
    }

    public void scanAndStrip(Player player) {
        boolean removed = stripContents(player, player.getInventory().getContents(), false);
        boolean removedEnder = stripContents(player, player.getEnderChest().getContents(), true);
        if (removed || removedEnder) {
            player.sendMessage(plugin.getMessages().get("expired-items-scan"));
        }
    }

    private boolean stripContents(Player player, ItemStack[] contents, boolean isEnder) {
        boolean removed = false;
        for (int i = 0; i < contents.length; i++) {
            String kitId = ItemUtil.getKitId(contents[i]);
            if (kitId != null && !plugin.getActiveKits().containsKey(kitId)) {
                if (isEnder) player.getEnderChest().setItem(i, null);
                else player.getInventory().setItem(i, null);
                removed = true;
            }
        }
        return removed;
    }
}
