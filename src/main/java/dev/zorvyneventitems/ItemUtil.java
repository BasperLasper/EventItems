package dev.zorvyneventitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static final String LORE_TAG_PREFIX = "§r§7[ZorvynEvent: ";
    private static NamespacedKey key;

    public static void init(JavaPlugin plugin) {
        key = new NamespacedKey(plugin, "event_kit_id");
    }

    public static NamespacedKey getKey() { return key; }

    /** Stamps the item with lore + PDC tag identifying the event kit */
    public static ItemStack stampItem(ItemStack original, String kitId, String kitCreator) {
        ItemStack stamped = original.clone();
        ItemMeta meta = stamped.getItemMeta();
        if (meta == null) return stamped;

        List<Component> lore = new ArrayList<>();
        if (meta.lore() != null) lore.addAll(meta.lore());

        lore.add(Component.empty());
        lore.add(Component.text("✦ ZorvynEvent Item", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Kit: " + kitId, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("By: " + kitCreator, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, kitId);
        stamped.setItemMeta(meta);
        return stamped;
    }

    /** Returns the kit ID embedded in the item, or null if not an event item */
    public static String getKitId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    public static boolean isEventItem(ItemStack item) {
        return getKitId(item) != null;
    }
}
