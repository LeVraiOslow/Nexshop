package fr.nexshop.hook;

import fr.nexshop.ShopPlugin;
import org.bukkit.Bukkit;

public final class EconomyFactory {
    private EconomyFactory() {}

    public static EconomyHook create(ShopPlugin plugin) {
        String mode = plugin.getConfig().getString("economy.provider", "auto").toLowerCase();

        if (!mode.equals("essentials") && Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                EconomyHook h = VaultEconomy.tryCreate();
                if (h != null) return h;
            } catch (Throwable t) {
                plugin.getLogger().warning("Vault : " + t.getMessage());
            }
        }
        if (!mode.equals("vault") && Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            try {
                return new EssentialsEconomy();
            } catch (Throwable t) {
                plugin.getLogger().warning("EssentialsX : " + t.getMessage());
            }
        }
        return null;
    }
}
