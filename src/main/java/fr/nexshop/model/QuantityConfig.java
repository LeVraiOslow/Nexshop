package fr.nexshop.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class QuantityConfig {
    public static final int ALL = -1;

    public record Button(String id, int amount, Icon icon, Set<Mode> modes) {
    }

    public final MenuTemplate buyTemplate;
    public final MenuTemplate sellTemplate;
    public final Icon preview;
    public final List<Button> buttons;
    public final boolean closeAfterTransaction;
    public final boolean closeOnError;

    private QuantityConfig(MenuTemplate buy, MenuTemplate sell, Icon preview, List<Button> buttons,
                           boolean closeAfter, boolean closeOnError) {
        this.buyTemplate = buy;
        this.sellTemplate = sell;
        this.preview = preview;
        this.buttons = buttons;
        this.closeAfterTransaction = closeAfter;
        this.closeOnError = closeOnError;
    }

    public static QuantityConfig load(YamlConfiguration y, Logger log) {
        MenuTemplate buy = MenuTemplate.load(y, "title-buy", "&8Acheter &7» &0%item_name%", 3);
        MenuTemplate sell = MenuTemplate.load(y, "title-sell", "&8Vendre &7» &0%item_name%", 3);
        ConfigurationSection p = y.getConfigurationSection("preview");
        Icon preview = p != null ? Icon.load(p, "%item_material%") : null;

        List<Button> buttons = new ArrayList<>();
        ConfigurationSection bs = y.getConfigurationSection("buttons");
        if (bs != null) {
            for (String key : bs.getKeys(false)) {
                ConfigurationSection b = bs.getConfigurationSection(key);
                if (b == null) continue;
                Object a = b.get("amount");
                int amount;
                if (a instanceof Number n) amount = Math.max(1, n.intValue());
                else if (a != null && a.toString().equalsIgnoreCase("all")) amount = ALL;
                else {
                    log.warning("quantity.yml : bouton '" + key + "' sans 'amount' valide.");
                    continue;
                }
                Set<Mode> modes = EnumSet.noneOf(Mode.class);
                List<String> raw = b.getStringList("modes");
                if (raw.isEmpty()) modes.addAll(EnumSet.allOf(Mode.class));
                for (String m : raw) {
                    try {
                        modes.add(Mode.valueOf(m.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                buttons.add(new Button(key, amount, Icon.load(b, "%item_material%"), modes));
            }
        }
        return new QuantityConfig(buy, sell, preview, buttons, y.getBoolean("close-after-transaction", false),
                y.getBoolean("close-on-error", false));
    }
}
