package fr.nexshop.service;

import fr.nexshop.ShopPlugin;
import fr.nexshop.hook.EconomyHook;
import fr.nexshop.model.Mode;
import fr.nexshop.model.ShopItem;
import fr.nexshop.util.Money;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ShopService {
    public static final int ALL = -1;
    private final ShopPlugin plugin;

    public ShopService(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ inventaire

    private boolean matches(ShopItem it, ItemStack s) {
        if (it.material == null || s == null || s.getType().isAir()) return false;
        String spec = it.material;
        if (spec.regionMatches(true, 0, "nexo:", 0, 5)) {
            String id = plugin.nexo().idOf(s);
            return id != null && id.equalsIgnoreCase(spec.substring(5));
        }
        Material m = Material.matchMaterial(spec);
        if (m == null || s.getType() != m) return false;
        if (plugin.nexo().idOf(s) != null) return false;
        if (!plugin.getConfig().getBoolean("sell.strict-vanilla-match", true)) return true;
        ItemMeta meta = s.getItemMeta();
        return meta == null || !(meta.hasDisplayName() || meta.hasLore());
    }

    public int owned(Player p, ShopItem it) {
        int total = 0;
        for (ItemStack s : p.getInventory().getStorageContents()) {
            if (matches(it, s)) total += s.getAmount();
        }
        return total;
    }

    public int freeSpace(Player p, ShopItem it) {
        ItemStack proto = plugin.items().create(it.material);
        if (proto == null) return 0;
        int max = proto.getMaxStackSize();
        int space = 0;
        for (ItemStack s : p.getInventory().getStorageContents()) {
            if (s == null || s.getType().isAir()) space += max;
            else if (s.isSimilar(proto)) space += Math.max(0, max - s.getAmount());
        }
        return space;
    }

    private void remove(Player p, ShopItem it, int amount) {
        ItemStack[] c = p.getInventory().getStorageContents();
        int left = amount;
        for (int i = 0; i < c.length && left > 0; i++) {
            ItemStack s = c[i];
            if (!matches(it, s)) continue;
            int take = Math.min(left, s.getAmount());
            if (take == s.getAmount()) c[i] = null;
            else s.setAmount(s.getAmount() - take);
            left -= take;
        }
        p.getInventory().setStorageContents(c);
    }

    private void give(Player p, ItemStack proto, int amount) {
        int max = proto.getMaxStackSize();
        int remaining = amount;
        while (remaining > 0) {
            int n = Math.min(max, remaining);
            ItemStack s = proto.clone();
            s.setAmount(n);
            p.getInventory().addItem(s).values()
                    .forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
            remaining -= n;
        }
    }

    // ------------------------------------------------------------------ calculs

    public int maxBuy(Player p, ShopItem it) {
        EconomyHook eco = plugin.economy();
        if (eco == null) return 0;
        long afford = it.buyPrice <= 0 ? Integer.MAX_VALUE
                : (long) Math.floor((eco.balance(p) + 1e-9) / it.buyPrice);
        long space = it.giveItem ? freeSpace(p, it) : plugin.getConfig().getInt("buy-all-cap-for-commands", 64);
        return (int) Math.max(0, Math.min(afford, space));
    }

    /** Résout la quantité réelle (gère "tout"). */
    public int resolve(Player p, ShopItem it, Mode mode, int requested) {
        if (requested != ALL) return requested;
        return mode == Mode.BUY ? maxBuy(p, it) : owned(p, it);
    }

    // ------------------------------------------------------------------ vérification d'ouverture

    private boolean ready(Player p) {
        if (plugin.economy() == null) {
            plugin.messages().send(p, "economy-unavailable");
            return false;
        }
        return true;
    }

    /** Vérifie qu'on peut ouvrir le menu de quantités. Envoie un message d'erreur sinon. */
    public boolean canOpen(Player p, ShopItem it, Mode mode) {
        if (!ready(p)) return false;
        var msg = plugin.messages();
        Money money = plugin.money();
        if (mode == Mode.BUY) {
            if (!it.buyable) {
                msg.send(p, "not-buyable", "item", it.displayName());
                return false;
            }
            double balance = plugin.economy().balance(p);
            if (balance + 1e-9 < it.buyPrice) {
                msg.send(p, "not-enough-money", "price", money.format(it.buyPrice),
                        "balance", money.format(balance), "item", it.displayName());
                return false;
            }
            if (it.giveItem && freeSpace(p, it) <= 0) {
                msg.send(p, "inventory-full", "item", it.displayName());
                return false;
            }
            return true;
        }
        if (!it.sellable) {
            msg.send(p, "not-sellable", "item", it.displayName());
            return false;
        }
        int have = owned(p, it);
        if (have <= 0) {
            msg.send(p, "not-enough-items", "item", it.displayName(), "have", have);
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ transaction

    public boolean execute(Player p, ShopItem it, Mode mode, int requested) {
        if (!ready(p)) return false;
        return mode == Mode.BUY ? buy(p, it, requested) : sell(p, it, requested);
    }

    private boolean buy(Player p, ShopItem it, int requested) {
        var msg = plugin.messages();
        var eco = plugin.economy();
        Money money = plugin.money();
        String name = it.displayName();

        if (!it.buyable) {
            msg.send(p, "not-buyable", "item", name);
            return false;
        }
        int amount = resolve(p, it, Mode.BUY, requested);
        if (amount <= 0) {
            double balance = eco.balance(p);
            if (balance + 1e-9 < it.buyPrice) {
                msg.send(p, "not-enough-money", "price", money.format(it.buyPrice),
                        "balance", money.format(balance), "item", name);
            } else {
                msg.send(p, "inventory-full", "item", name);
            }
            return false;
        }

        double total = Money.round2(it.buyPrice * amount);
        if (!eco.has(p, total)) {
            msg.send(p, "not-enough-money", "price", money.format(total),
                    "balance", money.format(eco.balance(p)), "item", name);
            return false;
        }

        ItemStack proto = null;
        if (it.giveItem) {
            proto = plugin.items().create(it.material);
            if (proto == null) {
                msg.send(p, "item-unavailable", "item", name);
                return false;
            }
            int space = freeSpace(p, it);
            if (space < amount) {
                if (space <= 0) msg.send(p, "inventory-full", "item", name);
                else msg.send(p, "not-enough-space", "max", space, "item", name, "amount", amount);
                return false;
            }
        }

        if (!eco.withdraw(p, total)) {
            msg.send(p, "transaction-failed");
            return false;
        }
        if (proto != null) give(p, proto, amount);
        runCommands(p, it, amount);
        msg.send(p, "bought", "amount", amount, "item", name, "total", money.format(total));
        return true;
    }

    private boolean sell(Player p, ShopItem it, int requested) {
        var msg = plugin.messages();
        var eco = plugin.economy();
        Money money = plugin.money();
        String name = it.displayName();

        if (!it.sellable) {
            msg.send(p, "not-sellable", "item", name);
            return false;
        }
        int have = owned(p, it);
        int amount = requested == ALL ? have : requested;
        if (have <= 0 || amount > have) {
            msg.send(p, "not-enough-items", "item", name, "have", have, "amount", amount);
            return false;
        }
        double total = Money.round2(it.sellPrice * amount);
        remove(p, it, amount);
        if (!eco.deposit(p, total)) {
            // remboursement des items si le dépôt échoue
            ItemStack proto = plugin.items().create(it.material);
            if (proto != null) give(p, proto, amount);
            msg.send(p, "transaction-failed");
            return false;
        }
        msg.send(p, "sold", "amount", amount, "item", name, "total", money.format(total));
        return true;
    }

    // ------------------------------------------------------------------ commandes

    private void runCommands(Player p, ShopItem it, int amount) {
        if (it.commands.isEmpty()) return;
        if (it.commandsPerUnit) {
            for (int i = 0; i < amount; i++) runCommands(p, it.commands, 1);
        } else {
            runCommands(p, it.commands, amount);
        }
    }

    public void runCommands(Player p, List<String> commands, int amount) {
        for (String raw : commands) {
            if (raw == null || raw.isBlank()) continue;
            String c = raw.trim();
            boolean asPlayer = false;
            if (c.regionMatches(true, 0, "[player]", 0, 8)) {
                asPlayer = true;
                c = c.substring(8).trim();
            } else if (c.regionMatches(true, 0, "[console]", 0, 9)) {
                c = c.substring(9).trim();
            }
            c = c.replace("%player%", p.getName()).replace("%amount%", String.valueOf(amount));
            if (c.startsWith("/")) c = c.substring(1);
            if (asPlayer) Bukkit.dispatchCommand(p, c);
            else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
        }
    }
}
