package dev.zorvyneventitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageUtil {

    private final ZorvynEventItems plugin;

    public MessageUtil(ZorvynEventItems plugin) { this.plugin = plugin; }

    /** Get a message from config, replace placeholders, return as Component */
    public Component get(String key, String... replacements) {
        String raw = plugin.getConfig().getString("messages." + key, "&cMissing message: " + key);

        // Apply placeholder pairs: replacements = {placeholder, value, placeholder, value...}
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }
}
