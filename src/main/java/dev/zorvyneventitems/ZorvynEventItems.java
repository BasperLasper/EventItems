package dev.zorvyneventitems;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public class ZorvynEventItems extends JavaPlugin {

    private final Map<String, EventKit> activeKits = new HashMap<>();
    private KitStorage kitStorage;

    @Override
    public void onEnable() {
        getLogger().info("ZorvynEventItems enabled!");
        saveDefaultConfig();
        ItemUtil.init(this);

        kitStorage = new KitStorage(this);
        activeKits.putAll(kitStorage.loadAll());

        getServer().getPluginManager().registerEvents(new EventItemListener(this), this);
        getCommand("eventitem").setExecutor(new EventItemCommand(this));
        getCommand("eventitem").setTabCompleter(new EventItemTabCompleter(this));
    }

    @Override
    public void onDisable() {
        kitStorage.saveAll(activeKits);
        getLogger().info("ZorvynEventItems disabled. Kits saved.");
    }

    public Map<String, EventKit> getActiveKits() { return activeKits; }

    /** Call this whenever a kit is created, claimed, or ended */
    public void saveKits() {
        kitStorage.saveAll(activeKits);
    }
}
