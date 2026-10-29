package fr.nexshop.model;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/** Titre, taille, remplissage et items décoratifs/boutons d'un menu. */
public final class MenuTemplate {
    public final String title;
    public final int rows;
    public final Icon filler;       // nullable
    public final List<Icon> extras; // décorations / boutons (action CLOSE, MAIN, BACK, commandes)

    private MenuTemplate(String title, int rows, Icon filler, List<Icon> extras) {
        this.title = title;
        this.rows = rows;
        this.filler = filler;
        this.extras = extras;
    }

    public static MenuTemplate load(ConfigurationSection s, String titleKey, String defTitle, int defRows) {
        String title = s.getString(titleKey, defTitle);
        int rows = Math.max(1, Math.min(6, s.getInt("rows", defRows)));
        Icon filler = null;
        ConfigurationSection f = s.getConfigurationSection("filler");
        if (f != null && f.getBoolean("enabled", true)) {
            filler = Icon.load(f, "BLACK_STAINED_GLASS_PANE");
        }
        List<Icon> extras = new ArrayList<>();
        ConfigurationSection items = s.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection c = items.getConfigurationSection(key);
                if (c != null) extras.add(Icon.load(c, "STONE"));
            }
        }
        return new MenuTemplate(title, rows, filler, extras);
    }
}
