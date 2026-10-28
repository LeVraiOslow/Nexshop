package fr.nexshop.menu;

import fr.nexshop.ShopPlugin;
import fr.nexshop.model.CategoryEntry;
import fr.nexshop.model.ShopCategory;
import fr.nexshop.util.Ph;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class MainMenu extends ShopMenu {

    public MainMenu(ShopPlugin plugin, Player player) {
        super(plugin, player, null);
    }

    @Override
    public void open() {
        var template = plugin.shop().mainTemplate();
        inventory = createInventory(template, Ph.of());
        handlers.clear();
        decorate(template, Ph.of());

        for (CategoryEntry entry : plugin.shop().entries()) {
            ItemStack item = plugin.items().build(entry.icon(), Ph.of("category", entry.id()), null);
            place(entry.icon(), item, e -> clickCategory(entry));
        }
        player.openInventory(inventory);
    }

    private void clickCategory(CategoryEntry entry) {
        if (entry.permission() != null && !entry.permission().isBlank() && !player.hasPermission(entry.permission())) {
            plugin.messages().send(player, "no-permission");
            plugin.playSound(player, "error");
            return;
        }
        ShopCategory category = plugin.shop().category(entry.id());
        if (category == null) {
            plugin.messages().send(player, "unknown-category");
            return;
        }
        later(() -> new CategoryMenu(plugin, player, category, this).open());
    }
}
