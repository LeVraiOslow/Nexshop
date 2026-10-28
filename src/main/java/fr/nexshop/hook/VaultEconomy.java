package fr.nexshop.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultEconomy implements EconomyHook {
    private final Economy eco;

    private VaultEconomy(Economy eco) {
        this.eco = eco;
    }

    public static VaultEconomy tryCreate() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : new VaultEconomy(rsp.getProvider());
    }

    @Override
    public String name() {
        return "Vault (" + eco.getName() + ")";
    }

    @Override
    public double balance(OfflinePlayer p) {
        return eco.getBalance(p);
    }

    @Override
    public boolean has(OfflinePlayer p, double amount) {
        return eco.has(p, amount);
    }

    @Override
    public boolean withdraw(OfflinePlayer p, double amount) {
        return eco.withdrawPlayer(p, amount).transactionSuccess();
    }

    @Override
    public boolean deposit(OfflinePlayer p, double amount) {
        return eco.depositPlayer(p, amount).transactionSuccess();
    }
}
