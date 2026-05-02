package dev.zorvyneventitems;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitStorage {

    private final ZorvynEventItems plugin;
    private final File file;
    private YamlConfiguration yaml;

    public KitStorage(ZorvynEventItems plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.yml");
    }

    public void saveAll(Map<String, EventKit> kits) {
        yaml = new YamlConfiguration();
        for (EventKit kit : kits.values()) {
            String path = "kits." + kit.getId();
            yaml.set(path + ".creator", kit.getCreatorName());

            // Save claimed UUIDs
            List<String> uuids = new ArrayList<>();
            for (UUID uuid : kit.getClaimedBy()) uuids.add(uuid.toString());
            yaml.set(path + ".claimed", uuids);

            // Save items as Bukkit serialized
            List<Map<String, Object>> serializedItems = new ArrayList<>();
            for (ItemStack item : kit.getItems()) {
                serializedItems.add(item.serialize());
            }
            yaml.set(path + ".items", serializedItems);
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kits.yml: " + e.getMessage());
        }
    }

    public Map<String, EventKit> loadAll() {
        Map<String, EventKit> kits = new HashMap<>();
        if (!file.exists()) return kits;

        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.isConfigurationSection("kits")) return kits;

        for (String id : yaml.getConfigurationSection("kits").getKeys(false)) {
            String path = "kits." + id;
            String creator = yaml.getString(path + ".creator", "Unknown");

            // Load claimed UUIDs
            Set<UUID> claimed = new HashSet<>();
            for (String uuidStr : yaml.getStringList(path + ".claimed")) {
                try { claimed.add(UUID.fromString(uuidStr)); }
                catch (IllegalArgumentException ignored) {}
            }

            // Load items
            List<ItemStack> items = new ArrayList<>();
            List<?> rawItems = yaml.getList(path + ".items");
            if (rawItems != null) {
                for (Object obj : rawItems) {
                    if (obj instanceof Map<?, ?> map) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> itemMap = (Map<String, Object>) map;
                            items.add(ItemStack.deserialize(itemMap));
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to deserialize item in kit '" + id + "': " + e.getMessage());
                        }
                    }
                }
            }

            EventKit kit = new EventKit(id, creator, items);
            kit.getClaimedBy().addAll(claimed);
            kits.put(id, kit);
        }
        plugin.getLogger().info("Loaded " + kits.size() + " kit(s) from kits.yml");
        return kits;
    }
}
