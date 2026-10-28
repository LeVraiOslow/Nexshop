package fr.nexshop;

import fr.nexshop.hook.EconomyFactory;
import fr.nexshop.hook.EconomyHook;
import fr.nexshop.hook.NexoHook;
import fr.nexshop.model.ShopManager;
import fr.nexshop.service.ItemFactory;
import fr.nexshop.service.ShopService;
import fr.nexshop.util.Messages;
import fr.nexshop.util.Money;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class ShopPlugin extends JavaPlugin {
    private static final List<String> DEFAULT_FILES = List.of(
            "config.yml", "messages.yml", "menus/main.yml", "menus/quantity.yml",
            "categories/blocs/category.yml", "categories/blocs/items.yml",
            "categories/cultures/category.yml", "categories/cultures/items.yml",
            "categories/nourritures/category.yml", "categories/nourritures/items.yml",
            "categories/minerais/category.yml", "categories/minerais/items.yml",
            "categories/redstone/category.yml", "categories/redstone/items.yml",
            "categories/spawners/category.yml", "categories/spawners/items.yml",
            "categories/loots/category.yml", "categories/loots/items.yml");

    private NexoHook nexo;
    private ItemFactory items;
    private Messages messages;
    private ShopManager shop;
    private ShopService service;
    private Money money;
    private EconomyHook economy;

    @Override
    public void onEnable() {
        for (String f : DEFAULT_FILES) {
            if (!new File(getDataFolder(), f).exists()) saveResource(f, false);
        }
        nexo = new NexoHook(this);
        items = new ItemFactory(this);
        messages = new Messages(this);
        shop = new ShopManager(this);
        service = new ShopService(this);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        ShopCommand cmd = new ShopCommand(this);
        var pc = getCommand("shop");
        if (pc != null) {
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }
        // Chargement au 1er tick : Vault / Essentials / Nexo sont alors tous prêts.
        Bukkit.getScheduler().runTask(this, this::reloadAll);
    }

    public void reloadAll() {
        reloadConfig();
        money = new Money(getConfig());
        messages.load();
        shop.load();
        economy = EconomyFactory.create(this);
        if (economy == null) {
            getLogger().warning("Aucune économie trouvée (Vault / EssentialsX). Le shop est désactivé.");
        } else {
            getLogger().info("Économie : " + economy.name());
        }
    }

    public void playSound(Player p, String key) {
        ConfigurationSection s = getConfig().getConfigurationSection("sounds." + key);
        if (s == null) return;
        String name = s.getString("sound", "");
        if (name.isBlank()) return;
        p.playSound(p.getLocation(), name, (float) s.getDouble("volume", 1.0), (float) s.getDouble("pitch", 1.0));
    }

    public NexoHook nexo() { return nexo; }
    public ItemFactory items() { return items; }
    public Messages messages() { return messages; }
    public ShopManager shop() { return shop; }
    public ShopService service() { return service; }
    public Money money() { return money; }
    public EconomyHook economy() { return economy; }
}
