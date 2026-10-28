package fr.nexshop.model;

import fr.nexshop.ShopPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/** Charge menus/main.yml, menus/quantity.yml et categories/<id>/*.yml. */
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
                ShopCategory category = loadCategory(dir, id, log);
                if (category == null) continue;
                String key = id.toLowerCase(Locale.ROOT);
                entries.put(key, new CategoryEntry(key, Icon.load(sec, "CHEST"), sec.getString("permission")));
                categories.put(key, category);
            }
        }
        quantity = QuantityConfig.load(YamlConfiguration.loadConfiguration(new File(dir, "menus/quantity.yml")), log);

        int items = categories.values().stream().mapToInt(c -> c.items().size()).sum();
        log.info(categories.size() + " catégories, " + items + " items chargés.");
    }

    /** categories/<id>/category.yml + tous les autres .yml du dossier (section shop-items). */
    private ShopCategory loadCategory(File dir, String id, Logger log) {
        String key = id.toLowerCase(Locale.ROOT);
        File folder = new File(dir, "categories/" + id);
        if (folder.isDirectory()) {
            File settingsFile = new File(folder, "category.yml");
            if (!settingsFile.exists()) {
                log.warning("Catégorie '" + id + "' : categories/" + id + "/category.yml introuvable.");
                return null;
            }
            File[] files = folder.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".yml")
                    && !n.equalsIgnoreCase("category.yml"));
            List<YamlConfiguration> others = new ArrayList<>();
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File f : files) others.add(YamlConfiguration.loadConfiguration(f));
            }
            return ShopCategory.load(key, YamlConfiguration.loadConfiguration(settingsFile), others, log);
        }
        // Ancien format : un seul fichier categories/<id>.yml
        File legacy = new File(dir, "categories/" + id + ".yml");
        if (legacy.exists()) {
            log.warning("Catégorie '" + id + "' : ancien format (fichier unique), pensez à créer un dossier categories/" + id + "/.");
            return ShopCategory.load(key, YamlConfiguration.loadConfiguration(legacy), List.of(), log);
        }
        log.warning("Catégorie '" + id + "' : dossier categories/" + id + "/ introuvable.");
        return null;
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
