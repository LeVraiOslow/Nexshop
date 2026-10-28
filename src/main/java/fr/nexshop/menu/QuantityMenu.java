package fr.nexshop.menu;

import fr.nexshop.ShopPlugin;
import fr.nexshop.model.Mode;
import fr.nexshop.model.QuantityConfig;
import fr.nexshop.model.ShopItem;
import fr.nexshop.util.Money;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class QuantityMenu extends ShopMenu {
    private final ShopItem item;
    private final Mode mode;

    public QuantityMenu(ShopPlugin plugin, Player player, ShopItem item, Mode mode, ShopMenu parent) {
        super(plugin, player, parent);
        this.item = item;
        this.mode = mode;
    }

    private Map<String, String> basePlaceholders() {
        Map<String, String> ph = new HashMap<>(plugin.items().shopPlaceholders(item));
        var money = plugin.money();
        ph.put("mode", plugin.messages().raw(mode == Mode.BUY ? "mode-buy" : "mode-sell"));
        ph.put("balance", money.format(plugin.economy() != null ? plugin.economy().balance(player) : 0));
        ph.put("owned", String.valueOf(item.material != null ? plugin.service().owned(player, item) : 0));
        ph.put("unit_price", money.format(mode == Mode.BUY ? item.buyPrice : item.sellPrice));
        return ph;
    }

    @Override
    public void open() {
        QuantityConfig q = plugin.shop().quantity();
        var template = mode == Mode.BUY ? q.buyTemplate : q.sellTemplate;
        inventory = createInventory(template, basePlaceholders());
        render();
        player.openInventory(inventory);
    }

    private void render() {
        QuantityConfig q = plugin.shop().quantity();
        var template = mode == Mode.BUY ? q.buyTemplate : q.sellTemplate;
        inventory.clear();
        handlers.clear();

        Map<String, String> ph = basePlaceholders();
        decorate(template, ph);

        if (q.preview != null) {
            Map<String, String> pv = new HashMap<>(ph);
            ItemStack stack = plugin.items().build(q.preview, pv, null);
            place(q.preview, stack, e -> {
            });
        }

        Money money = plugin.money();
        double unit = mode == Mode.BUY ? item.buyPrice : item.sellPrice;
        for (QuantityConfig.Button b : q.buttons) {
            if (!b.modes().contains(mode)) continue;
            int amount = plugin.service().resolve(player, item, mode, b.amount());
            Map<String, String> p2 = new HashMap<>(ph);
            p2.put("amount", String.valueOf(Math.max(0, amount)));
            p2.put("total", money.format(Money.round2(unit * Math.max(0, amount))));
            ItemStack stack = plugin.items().build(b.icon(), p2, null);
            place(b.icon(), stack, e -> click(b));
        }
    }

    private void click(QuantityConfig.Button b) {
        QuantityConfig q = plugin.shop().quantity();
        boolean ok = plugin.service().execute(player, item, mode, b.amount());
        plugin.playSound(player, ok ? "success" : "error");
        if (ok ? q.closeAfterTransaction : q.closeOnError) {
            later(player::closeInventory);
        } else if (ok) {
            render();
        }
    }
}
