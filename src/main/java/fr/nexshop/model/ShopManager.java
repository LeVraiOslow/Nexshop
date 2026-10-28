package fr.nexshop.model;

import fr.nexshop.ShopPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/** Charge menus/main.yml, menus/quantity.yml et categories/*.yml. */
public final class ShopManager {
    private final ShopPlugin plugin;
    private MenuTemplate mainTemplate;
    private QuantityConfig quantity;
    private final Map<String, CategoryEntry> entries = new LinkedHashMap<>();
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File dir = plugin.getDataFolder();
        Logger log = plugin.getLogger();
        entries.clear();
        categories.clear();

        YamlConfiguration main = YamlConfiguration.loadConfiguration(new File(dir, "menus/main.yml"));
        mainTemplate = MenuTemplate.load(main, "title", "&8Shop", 3);
        ConfigurationSection cs = main.getConfigurationSection("categories");
        if (cs != null) {
            for (String id : cs.getKeys(false)) {
                ConfigurationSection sec = cs.getConfigurationSection(id);
                if (sec == null) continue;
                File f = new File(dir, "categories/" + id + ".yml");
                if (!f.exists()) {
                    log.warning("Catégorie '" + id + "' : fichier categories/" + id + ".yml introuvable.");
                    continue;
                }
                String key = id.toLowerCase(Locale.ROOT);
                entries.put(key, new CategoryEntry(key, Icon.load(sec, "CHEST"), sec.getString("permission")));
                categories.put(key, ShopCategory.load(key, YamlConfiguration.loadConfiguration(f), log));
            }
        }
        quantity = QuantityConfig.load(YamlConfiguration.loadConfiguration(new File(dir, "menus/quantity.yml")), log);

        int items = categories.values().stream().mapToInt(c -> c.items().size()).sum();
        log.info(categories.size() + " catégories, " + items + " items chargés.");
    }

    public MenuTemplate mainTemplate() {
        return mainTemplate;
    }

    public QuantityConfig quantity() {
        return quantity;
    }

    public Collection<CategoryEntry> entries() {
        return entries.values();
    }

    public CategoryEntry entry(String id) {
        return id == null ? null : entries.get(id.toLowerCase(Locale.ROOT));
    }

    public ShopCategory category(String id) {
        return id == null ? null : categories.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<String> categoryIds() {
        return categories.keySet();
    }
}
