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

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (!sender.hasPermission("zorvyneventitems.create")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED)); return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Players only.", NamedTextColor.RED)); return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /eventitem create <id>", NamedTextColor.RED)); return true;
                }
                String id = args[1].toLowerCase();
                if (plugin.getActiveKits().containsKey(id)) {
                    sender.sendMessage(Component.text("A kit with that ID already exists!", NamedTextColor.RED)); return true;
                }
                List<ItemStack> items = new ArrayList<>();
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && !item.getType().isAir()) {
                        items.add(ItemUtil.stampItem(item, id, player.getName()));
                    }
                }
                if (items.isEmpty()) {
                    sender.sendMessage(Component.text("Your inventory is empty!", NamedTextColor.RED)); return true;
                }
                EventKit kit = new EventKit(id, player.getName(), items);
                plugin.getActiveKits().put(id, kit);
                plugin.saveKits();
                sender.sendMessage(Component.text("✦ Event kit '", NamedTextColor.GREEN)
                        .append(Component.text(id, NamedTextColor.YELLOW))
                        .append(Component.text("' created with " + items.size() + " item(s)!", NamedTextColor.GREEN)));
            }

            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Players only.", NamedTextColor.RED)); return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /eventitem claim <id>", NamedTextColor.RED)); return true;
                }
                String id = args[1].toLowerCase();
                EventKit kit = plugin.getActiveKits().get(id);
                if (kit == null) {
                    sender.sendMessage(Component.text("No active kit with that ID.", NamedTextColor.RED)); return true;
                }
                if (kit.hasClaimed(player.getUniqueId())) {
                    sender.sendMessage(Component.text("You have already claimed this kit!", NamedTextColor.RED)); return true;
                }
                for (ItemStack item : kit.getItems()) {
                    player.getInventory().addItem(item.clone());
                }
                kit.addClaimed(player.getUniqueId());
                plugin.saveKits();
                player.sendMessage(Component.text("✦ You claimed event kit '", NamedTextColor.GREEN)
                        .append(Component.text(id, NamedTextColor.YELLOW))
                        .append(Component.text("'!", NamedTextColor.GREEN)));
            }

            case "end" -> {
                if (!sender.hasPermission("zorvyneventitems.end")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED)); return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /eventitem end <id>", NamedTextColor.RED)); return true;
                }
                String id = args[1].toLowerCase();
                EventKit kit = plugin.getActiveKits().get(id);
                if (kit == null) {
                    sender.sendMessage(Component.text("No active kit with that ID.", NamedTextColor.RED)); return true;
                }
                int totalRemoved = 0;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    totalRemoved += removeKitItems(online, id);
                }
                plugin.getActiveKits().remove(id);
                plugin.saveKits();
                sender.sendMessage(Component.text("✦ Kit '", NamedTextColor.GOLD)
                        .append(Component.text(id, NamedTextColor.YELLOW))
                        .append(Component.text("' ended. Removed " + totalRemoved + " item(s) from online players.", NamedTextColor.GOLD)));
                Bukkit.broadcast(Component.text("✦ Event kit '", NamedTextColor.RED)
                        .append(Component.text(id, NamedTextColor.YELLOW))
                        .append(Component.text("' has ended. Items removed.", NamedTextColor.RED)));
            }

            case "list" -> {
                Map<String, EventKit> kits = plugin.getActiveKits();
                if (kits.isEmpty()) {
                    sender.sendMessage(Component.text("No active event kits.", NamedTextColor.GRAY));
                } else {
                    sender.sendMessage(Component.text("Active kits: ", NamedTextColor.GOLD)
                            .append(Component.text(String.join(", ", kits.keySet()), NamedTextColor.YELLOW)));
                }
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private int removeKitItems(Player player, String kitId) {
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
            player.sendMessage(Component.text("✦ " + count + " event item(s) were removed from your inventory.", NamedTextColor.RED));
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
