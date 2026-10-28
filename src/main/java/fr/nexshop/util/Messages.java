package fr.nexshop.util;

import fr.nexshop.ShopPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class Messages {
    private final ShopPlugin plugin;
    private YamlConfiguration yaml = new YamlConfiguration();

    public Messages(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        yaml = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        InputStream def = plugin.getResource("messages.yml");
        if (def != null) {
            yaml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(def, StandardCharsets.UTF_8)));
        }
    }

    public String raw(String key) {
        return yaml.getString(key, key);
    }

    public void send(CommandSender to, String key, Object... kv) {
        Map<String, String> ph = Ph.of(kv);
        ph.put("prefix", yaml.getString("prefix", ""));
        List<String> lines = yaml.isList(key)
                ? yaml.getStringList(key)
                : List.of(yaml.getString(key, "&cMessage manquant : " + key));
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            to.sendMessage(TextUtil.parse(Ph.apply(line, ph)));
        }
    }
}
