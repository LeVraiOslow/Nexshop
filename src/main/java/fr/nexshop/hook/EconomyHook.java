package fr.nexshop.hook;

import org.bukkit.OfflinePlayer;

public interface EconomyHook {
    String name();

    double balance(OfflinePlayer player);

    boolean has(OfflinePlayer player, double amount);

    boolean withdraw(OfflinePlayer player, double amount);

    boolean deposit(OfflinePlayer player, double amount);
}
