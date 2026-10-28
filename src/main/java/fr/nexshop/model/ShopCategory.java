package fr.nexshop.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** Une catégorie : menu + items répartis automatiquement sur plusieurs pages. */
public final class ShopCategory {
    private final String id;
    private final MenuTemplate template;
    private final List<ShopItem> items;
    private final List<Map<Integer, ShopItem>> pages;

    private ShopCategory(String id, MenuTemplate template, List<ShopItem> items, List<Map<Integer, ShopItem>> pages) {
        this.id = id;
        this.template = template;
        this.items = items;
        this.pages = pages;
    }

    public String id() {
        return id;
    }

    public MenuTemplate template() {
        return template;
    }

    public List<ShopItem> items() {
        return items;
    }

    public int pageCount() {
        return pages.size();
    }

    /** Page 1-indexée : slot -> item. */
    public Map<Integer, ShopItem> page(int page) {
        return pages.get(Math.max(1, Math.min(page, pages.size())) - 1);
    }

    /**
     * @param settings category.yml (titre, rows, content-slots, filler, items, + éventuellement shop-items)
     * @param others   les autres fichiers .yml du dossier (section shop-items)
     */
    public static ShopCategory load(String id, YamlConfiguration settings, List<YamlConfiguration> others, Logger log) {
        MenuTemplate t = MenuTemplate.load(settings, "title", "&8Shop", 6);
        int size = t.rows * 9;

        List<ShopItem> list = new ArrayList<>();
        List<YamlConfiguration> all = new ArrayList<>();
        all.add(settings);
        all.addAll(others);
        for (YamlConfiguration y : all) {
            ConfigurationSection s = y.getConfigurationSection("shop-items");
            if (s == null) continue;
            for (String key : s.getKeys(false)) {
                ConfigurationSection c = s.getConfigurationSection(key);
                if (c == null) continue;
                ShopItem it = ShopItem.load(id + "." + key, c, log);
                if (it != null) list.add(it);
            }
        }

        // Emplacements automatiques : content-slots, sinon tout sauf la dernière ligne (réservée à la navigation)
        List<Integer> content = new ArrayList<>();
        if (settings.contains("content-slots")) {
            for (int slot : Icon.parseSlotList(settings.get("content-slots"))) {
                if (slot >= 0 && slot < size && !content.contains(slot)) content.add(slot);
            }
        } else {
            int limit = t.rows >= 2 ? (t.rows - 1) * 9 : size;
            for (int i = 0; i < limit; i++) content.add(i);
        }

        List<Map<Integer, ShopItem>> pages = new ArrayList<>();
        pages.add(new LinkedHashMap<>());

        // 1) items à emplacement forcé
        for (ShopItem it : list) {
            if (it.icon.slots.isEmpty()) continue;
            Map<Integer, ShopItem> m = pageMap(pages, it.page);
            for (int slot : it.icon.slots) {
                if (slot < 0 || slot >= size) {
                    log.warning("Item '" + it.id + "' : slot " + slot + " hors du menu (" + size + " slots).");
                    continue;
                }
                m.put(slot, it);
            }
        }

        // 2) items automatiques, répartis sur les pages
        int page = 1;
        int idx = 0;
        for (ShopItem it : list) {
            if (!it.icon.slots.isEmpty()) continue;
            if (content.isEmpty()) {
                log.warning("Catégorie '" + id + "' : aucun content-slot, item '" + it.id + "' ignoré.");
                continue;
            }
            while (true) {
                Map<Integer, ShopItem> m = pageMap(pages, page);
                while (idx < content.size() && m.containsKey(content.get(idx))) idx++;
                if (idx < content.size()) {
                    m.put(content.get(idx++), it);
                    break;
                }
                page++;
                idx = 0;
            }
        }
        return new ShopCategory(id, t, list, pages);
    }

    private static Map<Integer, ShopItem> pageMap(List<Map<Integer, ShopItem>> pages, int page) {
        while (pages.size() < page) pages.add(new LinkedHashMap<>());
        return pages.get(page - 1);
    }
}
