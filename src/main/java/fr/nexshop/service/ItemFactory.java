package fr.nexshop.service;

import fr.nexshop.ShopPlugin;
import fr.nexshop.model.Icon;
import fr.nexshop.model.ShopItem;
import fr.nexshop.util.Ph;
import fr.nexshop.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;
import java.util.Map;

public final class ItemFactory {
    private final ShopPlugin plugin;

    public ItemFactory(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    /** Crée un item à partir de "MATERIAL" ou "nexo:id". Retourne null si invalide. */
    public ItemStack create(String spec) {
        if (spec == null || spec.isBlank()) return null;
        if (spec.regionMatches(true, 0, "nexo:", 0, 5)) {
            return plugin.nexo().build(spec.substring(5));
        }
        Material m = Material.matchMaterial(spec.trim());
        if (m == null || !m.isItem() || m.isAir()) return null;
        return new ItemStack(m);
    }

    public ItemStack build(Icon icon, Map<String, String> ph, List<String> fallbackLore) {
        String spec = Ph.apply(icon.material, ph);
        ItemStack stack = create(spec);
        if (stack == null) {
            plugin.getLogger().warning("Item introuvable : '" + spec + "' (BARRIER utilisé à la place)");
            stack = new ItemStack(Material.BARRIER);
        }
        stack.setAmount(Math.max(1, Math.min(icon.amount, stack.getMaxStackSize())));
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (icon.name != null) meta.displayName(TextUtil.parse(Ph.apply(icon.name, ph)));
            List<String> lore = icon.lore != null ? icon.lore : fallbackLore;
            if (lore != null) {
                meta.lore(lore.stream().map(l -> TextUtil.parse(Ph.apply(l, ph))).toList());
            }
            if (icon.glow) meta.setEnchantmentGlintOverride(true);
            if (icon.customModelData != null) {
                CustomModelDataComponent c = meta.getCustomModelDataComponent();
                c.setFloats(List.of(icon.customModelData.floatValue()));
                meta.setCustomModelDataComponent(c);
            }
            if (icon.hideFlags) meta.addItemFlags(ItemFlag.values());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public Map<String, String> shopPlaceholders(ShopItem it) {
        FileConfiguration cfg = plugin.getConfig();
        var money = plugin.money();
        String buyPrice = it.buyable ? money.format(it.buyPrice) : cfg.getString("item-lore.price-disabled", "&cN/A");
        String sellPrice = it.sellable ? money.format(it.sellPrice) : cfg.getString("item-lore.price-disabled", "&cN/A");
        String buyLine = it.buyable
                ? cfg.getString("item-lore.buy", "&aAchat : &f%price%").replace("%price%", buyPrice)
                : cfg.getString("item-lore.not-buyable", "&cAchat indisponible");
        String sellLine = it.sellable
                ? cfg.getString("item-lore.sell", "&cVente : &f%price%").replace("%price%", sellPrice)
                : cfg.getString("item-lore.not-sellable", "&cVente indisponible");
        return Ph.of("item_name", it.displayName(), "buy_price", buyPrice, "sell_price", sellPrice,
                "buy_line", buyLine, "sell_line", sellLine, "item_material", it.icon.material);
    }

    public ItemStack buildShopItem(ShopItem it) {
        return build(it.icon, shopPlaceholders(it), plugin.getConfig().getStringList("default-item-lore"));
    }
}
