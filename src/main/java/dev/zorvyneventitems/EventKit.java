package dev.zorvyneventitems;

import org.bukkit.inventory.ItemStack;
import java.util.*;

public class EventKit {
    private final String id;
    private final String creatorName;
    private final List<ItemStack> items;
    private final Set<UUID> claimedBy = new HashSet<>();

    public EventKit(String id, String creatorName, List<ItemStack> items) {
        this.id = id;
        this.creatorName = creatorName;
        this.items = items;
    }

    public String getId() { return id; }
    public String getCreatorName() { return creatorName; }
    public List<ItemStack> getItems() { return items; }
    public Set<UUID> getClaimedBy() { return claimedBy; }

    public boolean hasClaimed(UUID uuid) { return claimedBy.contains(uuid); }
    public void addClaimed(UUID uuid) { claimedBy.add(uuid); }
}
