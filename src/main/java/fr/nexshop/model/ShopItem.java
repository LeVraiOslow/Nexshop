package fr.nexshop.model;

import fr.nexshop.util.Ph;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.logging.Logger;

public final class ShopItem {
    public final String id;
    public final Icon icon;          // affichage dans le menu (icon.slots = emplacements forcés, vide = automatique)
    public final int page;           // page de l'emplacement forcé (défaut 1)
    public final String material;    // item réellement donné / repris (null si give-item=false)
    public final double buyPrice;
    public final double sellPrice;
    public final boolean buyable;
    public final boolean sellable;
    public final List<String> commands;
    public final boolean giveItem;
    public final boolean commandsPerUnit;

    private ShopItem(String id, Icon icon, int page, String material, double buy, double sell, boolean buyable,
                     boolean sellable, List<String> commands, boolean giveItem, boolean perUnit) {
        this.id = id;
        this.icon = icon;
        this.page = page;
        this.material = material;
        this.buyPrice = buy;
        this.sellPrice = sell;
        this.buyable = buyable;
        this.sellable = sellable;
        this.commands = commands;
        this.giveItem = giveItem;
        this.commandsPerUnit = perUnit;
    }

    public static ShopItem load(String id, ConfigurationSection s, Logger log) {
        String material = s.getString("material");
        List<String> commands = s.getStringList("commands");
        boolean give = s.getBoolean("give-item", commands.isEmpty());
        if (material == null) {
            log.warning("Item '" + id + "' : 'material' manquant, ignoré.");
            return null;
        }
        double buy = s.getDouble("buy-price", -1);
        double sell = s.getDouble("sell-price", -1);
        boolean buyable = s.getBoolean("buyable", true) && buy >= 0;
        boolean sellable = s.getBoolean("sellable", true) && sell >= 0 && give;
        if (!give && s.getDouble("sell-price", -1) >= 0) {
            log.warning("Item '" + id + "' : give-item=false, la vente est désactivée.");
        }
        Icon icon = Icon.load(s, material);
        return new ShopItem(id, icon, Math.max(1, s.getInt("page", 1)), give ? material : null, buy, sell,
                buyable, sellable, commands, give, s.getBoolean("commands-per-unit", false));
    }

    public String displayName() {
        return icon.name != null ? icon.name : Ph.pretty(icon.material);
    }
}
