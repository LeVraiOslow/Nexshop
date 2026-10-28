package fr.nexshop.menu;

import fr.nexshop.ShopPlugin;
import fr.nexshop.model.Icon;
import fr.nexshop.model.Mode;
import fr.nexshop.model.ShopCategory;
import fr.nexshop.model.ShopItem;
import fr.nexshop.util.Ph;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class CategoryMenu extends ShopMenu {
    private final ShopCategory category;
    private int page = 1;

    public CategoryMenu(ShopPlugin plugin, Player player, ShopCategory category, ShopMenu parent) {
        super(plugin, player, parent);
        this.category = category;
    }

    @Override
    public void open() {
        int pages = category.pageCount();
        page = Math.max(1, Math.min(page, pages));
        var ph = Ph.of("category", category.id(), "page", page, "pages", pages);
        inventory = createInventory(category.template(), ph);
        handlers.clear();
        decorate(category.template(), ph);

        for (Map.Entry<Integer, ShopItem> e : category.page(page).entrySet()) {
            ShopItem item = e.getValue();
            ItemStack stack = plugin.items().buildShopItem(item);
            setSlot(e.getKey(), stack, ev -> {
                ClickType t = ev.getClick();
                if (t.isLeftClick()) openQuantity(item, Mode.BUY);        // clic gauche = acheter
                else if (t.isRightClick()) openQuantity(item, Mode.SELL); // clic droit  = vendre
            });
        }
        player.openInventory(inventory);
    }

    @Override
    protected boolean visible(Icon icon) {
        String a = icon.action == null ? "" : icon.action.toUpperCase();
        return switch (a) {
            case "PREVIOUS_PAGE", "PREV_PAGE" -> page > 1;
            case "NEXT_PAGE" -> page < category.pageCount();
            default -> true;
        };
    }

    @Override
    protected void changePage(int delta) {
        int target = Math.max(1, Math.min(page + delta, category.pageCount()));
        if (target == page) return;
        page = target;
        later(this::open);
    }

    private void openQuantity(ShopItem item, Mode mode) {
        if (!plugin.service().canOpen(player, item, mode)) {
            plugin.playSound(player, "error");
            return;
        }
        later(() -> new QuantityMenu(plugin, player, item, mode, this).open());
    }
}
