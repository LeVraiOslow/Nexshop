package fr.nexshop.menu;

import fr.nexshop.ShopPlugin;
import fr.nexshop.model.Mode;
import fr.nexshop.model.ShopCategory;
import fr.nexshop.model.ShopItem;
import fr.nexshop.util.Ph;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class CategoryMenu extends ShopMenu {
    private final ShopCategory category;

    public CategoryMenu(ShopPlugin plugin, Player player, ShopCategory category, ShopMenu parent) {
        super(plugin, player, parent);
        this.category = category;
    }

    @Override
    public void open() {
        var ph = Ph.of("category", category.id());
        inventory = createInventory(category.template(), ph);
        handlers.clear();
        decorate(category.template(), ph);

        for (ShopItem item : category.items()) {
            ItemStack stack = plugin.items().buildShopItem(item);
            place(item.icon, stack, e -> {
                ClickType t = e.getClick();
                if (t.isLeftClick()) openQuantity(item, Mode.SELL);   // clic gauche = vendre
                else if (t.isRightClick()) openQuantity(item, Mode.BUY); // clic droit = acheter
            });
        }
        player.openInventory(inventory);
    }

    private void openQuantity(ShopItem item, Mode mode) {
        if (!plugin.service().canOpen(player, item, mode)) {
            plugin.playSound(player, "error");
            return;
        }
        later(() -> new QuantityMenu(plugin, player, item, mode, this).open());
    }
}
