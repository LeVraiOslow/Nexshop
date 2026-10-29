package fr.nexshop.menu;

import fr.nexshop.ShopPlugin;
import fr.nexshop.model.Icon;
import fr.nexshop.model.MenuTemplate;
import fr.nexshop.util.Ph;
import fr.nexshop.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ShopMenu implements InventoryHolder {
    protected final ShopPlugin plugin;
    protected final Player player;
    protected final ShopMenu parent;
    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    protected ShopMenu(ShopPlugin plugin, Player player, ShopMenu parent) {
        this.plugin = plugin;
        this.player = player;
        this.parent = parent;
    }

    public abstract void open();

    public Player player() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(InventoryClickEvent e) {
        Consumer<InventoryClickEvent> h = handlers.get(e.getRawSlot());
        if (h == null) return;
        plugin.playSound(player, "click");
        h.accept(e);
    }

    protected Inventory createInventory(MenuTemplate t, Map<String, String> ph) {
        return Bukkit.createInventory(this, t.rows * 9, TextUtil.parse(Ph.apply(t.title, ph)));
    }

    /** Remplissage + items décoratifs / boutons d'action. */
    protected void decorate(MenuTemplate t, Map<String, String> ph) {
        var factory = plugin.items();
        if (t.filler != null) {
            ItemStack filler = factory.build(t.filler, ph, null);
            for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler.clone());
        }
        for (Icon icon : t.extras) {
            if (!visible(icon)) continue;
            ItemStack item = factory.build(icon, ph, null);
            for (int slot : icon.slots) {
                if (slot < 0 || slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item.clone());
                handlers.put(slot, e -> runAction(icon));
            }
        }
    }

    protected void place(Icon icon, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int slot : icon.slots) {
            if (slot < 0 || slot >= inventory.getSize()) {
                plugin.getLogger().warning("Slot invalide (" + slot + ") dans un menu.");
                continue;
            }
            inventory.setItem(slot, item.clone());
            handlers.put(slot, handler);
        }
    }

    protected void runAction(Icon icon) {
        String a = icon.action == null ? "" : icon.action.toUpperCase();
        switch (a) {
            case "CLOSE" -> later(player::closeInventory);
            case "PREVIOUS_PAGE", "PREV_PAGE" -> changePage(-1);
            case "NEXT_PAGE" -> changePage(1);
            case "MAIN" -> later(() -> new MainMenu(plugin, player).open());
            case "BACK" -> later(() -> {
                if (parent != null) parent.open();
                else new MainMenu(plugin, player).open();
            });
            default -> {
            }
        }
        List<String> cmds = icon.commands;
        if (cmds != null && !cmds.isEmpty()) plugin.service().runCommands(player, cmds, 1);
    }

    /** Permet à un menu de masquer certains boutons décoratifs (ex : page précédente sur la page 1). */
    protected boolean visible(Icon icon) {
        return true;
    }

    /** Appelé par les actions PREVIOUS_PAGE / NEXT_PAGE. */
    protected void changePage(int delta) {
    }

    protected void setSlot(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        if (slot < 0 || slot >= inventory.getSize()) {
            plugin.getLogger().warning("Slot invalide (" + slot + ") dans un menu.");
            return;
        }
        inventory.setItem(slot, item.clone());
        handlers.put(slot, handler);
    }

    protected void later(Runnable r) {
        Bukkit.getScheduler().runTask(plugin, r);
    }
}
