package fr.nexshop.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public record ShopCategory(String id, MenuTemplate template, List<ShopItem> items) {

    public static ShopCategory load(String id, YamlConfiguration y, Logger log) {
        MenuTemplate t = MenuTemplate.load(y, "title", "&8Shop", 6);
        List<ShopItem> list = new ArrayList<>();
        ConfigurationSection s = y.getConfigurationSection("shop-items");
        if (s != null) {
            for (String key : s.getKeys(false)) {
                ConfigurationSection c = s.getConfigurationSection(key);
                if (c == null) continue;
                ShopItem it = ShopItem.load(id + "." + key, c, log);
                if (it != null) list.add(it);
            }
        }
        return new ShopCategory(id, t, list);
    }
}
