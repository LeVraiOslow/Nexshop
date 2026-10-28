package fr.nexshop;

import fr.nexshop.menu.CategoryMenu;
import fr.nexshop.menu.MainMenu;
import fr.nexshop.model.CategoryEntry;
import fr.nexshop.model.ShopCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class ShopCommand implements TabExecutor {
    private final ShopPlugin plugin;

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("shop.admin")) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }
            plugin.reloadAll();
            plugin.messages().send(sender, "reloaded");
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (!player.hasPermission("shop.use")) {
            plugin.messages().send(player, "no-permission");
            return true;
        }
        if (plugin.economy() == null) {
            plugin.messages().send(player, "economy-unavailable");
            return true;
        }
        MainMenu main = new MainMenu(plugin, player);
        if (args.length >= 1) {
            CategoryEntry entry = plugin.shop().entry(args[0]);
            ShopCategory category = plugin.shop().category(args[0]);
            if (entry == null || category == null) {
                plugin.messages().send(player, "unknown-category");
                return true;
            }
            if (entry.permission() != null && !entry.permission().isBlank() && !player.hasPermission(entry.permission())) {
                plugin.messages().send(player, "no-permission");
                return true;
            }
            new CategoryMenu(plugin, player, category, main).open();
            return true;
        }
        main.open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        List<String> options = new ArrayList<>(plugin.shop().categoryIds());
        if (sender.hasPermission("shop.admin")) options.add("reload");
        return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
    }
}
