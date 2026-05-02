package dev.zorvyneventitems;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventItemTabCompleter implements TabCompleter {

    private final ZorvynEventItems plugin;

    public EventItemTabCompleter(ZorvynEventItems plugin) { this.plugin = plugin; }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("create", "claim", "end", "list");
        if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
            return new ArrayList<>(plugin.getActiveKits().keySet());
        }
        return List.of();
    }
}
