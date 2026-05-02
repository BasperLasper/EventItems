package dev.zorvyneventitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventItemCommand implements CommandExecutor {

    private final ZorvynEventItems plugin;

    public EventItemCommand(ZorvynEventItems plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        MessageUtil msg = plugin.getMessages();
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (!sender.hasPermission("zorvyneventitems.create")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(msg.get("players-only")); return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(msg.get("usage-create")); return true;
                }
                String id = args[1].toLowerCase();
                if (plugin.getActiveKits().containsKey(id)) {
                    sender.sendMessage(msg.get("kit-already-exists", "id", id)); return true;
                }
                List<ItemStack> items = new ArrayList<>();
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && !item.getType().isAir()) {
                        items.add(ItemUtil.stampItem(item, id, player.getName()));
                    }
                }
                if (items.isEmpty()) {
                    sender.sendMessage(msg.get("inventory-empty")); return true;
                }
                EventKit kit = new EventKit(id, player.getName(), items);
                plugin.getActiveKits().put(id, kit);
                plugin.saveKits();
                sender.sendMessage(msg.get("kit-created", "id", id, "count", String.valueOf(items.size())));
            }

            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(msg.get("players-only")); return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(msg.get("usage-claim")); return true;
                }
                String id = args[1].toLowerCase();
                EventKit kit = plugin.getActiveKits().get(id);
                if (kit == null) {
                    sender.sendMessage(msg.get("kit-not-found", "id", id)); return true;
                }
                if (kit.hasClaimed(player.getUniqueId())) {
                    sender.sendMessage(msg.get("already-claimed", "id", id)); return true;
                }
                for (ItemStack item : kit.getItems()) {
                    player.getInventory().addItem(item.clone());
                }
                kit.addClaimed(player.getUniqueId());
                plugin.saveKits();
                player.sendMessage(msg.get("kit-claimed", "id", id));
            }

            case "end" -> {
                if (!sender.hasPermission("zorvyneventitems.end")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(msg.get("usage-end")); return true;
                }
                String id = args[1].toLowerCase();
                EventKit kit = plugin.getActiveKits().get(id);
                if (kit == null) {
                    sender.sendMessage(msg.get("kit-not-found", "id", id)); return true;
                }
                int totalRemoved = 0;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    totalRemoved += removeKitItems(online, id, msg);
                }
                plugin.getActiveKits().remove(id);
                plugin.saveKits();
                sender.sendMessage(msg.get("kit-ended-sender", "id", id, "count", String.valueOf(totalRemoved)));
                Bukkit.broadcast(msg.get("kit-ended-broadcast", "id", id));
            }

            case "list" -> {
                Map<String, EventKit> kits = plugin.getActiveKits();
                if (kits.isEmpty()) {
                    sender.sendMessage(msg.get("no-active-kits"));
                } else {
                    sender.sendMessage(msg.get("active-kits", "kits", String.join(", ", kits.keySet())));
                }
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private int removeKitItems(Player player, String kitId, MessageUtil msg) {
        int count = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (kitId.equals(ItemUtil.getKitId(contents[i]))) {
                player.getInventory().setItem(i, null);
                count++;
            }
        }
        ItemStack[] ender = player.getEnderChest().getContents();
        for (int i = 0; i < ender.length; i++) {
            if (kitId.equals(ItemUtil.getKitId(ender[i]))) {
                player.getEnderChest().setItem(i, null);
                count++;
            }
        }
        if (count > 0) {
            player.sendMessage(msg.get("items-removed-player", "count", String.valueOf(count)));
        }
        return count;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("── ZorvynEventItems ──", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/eventitem create <id>", NamedTextColor.YELLOW)
                .append(Component.text(" — Create kit from your inventory", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/eventitem claim <id>", NamedTextColor.YELLOW)
                .append(Component.text(" — Claim a kit", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/eventitem end <id>", NamedTextColor.YELLOW)
                .append(Component.text(" — End kit & remove items from all players", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/eventitem list", NamedTextColor.YELLOW)
                .append(Component.text(" — List active kits", NamedTextColor.GRAY)));
    }
}
