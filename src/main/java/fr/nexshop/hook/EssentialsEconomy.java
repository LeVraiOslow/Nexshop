package fr.nexshop.hook;

import org.bukkit.OfflinePlayer;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;

/** Économie EssentialsX directe (par réflexion), sans passer par Vault. */
public final class EssentialsEconomy implements EconomyHook {
    private final Method getMoney;
    private final Method hasEnough;
    private final Method add;
    private final Method subtract;

    public EssentialsEconomy() throws Exception {
        Class<?> c = Class.forName("com.earth2me.essentials.api.Economy");
        getMoney = c.getMethod("getMoneyExact", UUID.class);
        hasEnough = c.getMethod("hasEnough", UUID.class, BigDecimal.class);
        add = c.getMethod("add", UUID.class, BigDecimal.class);
        subtract = c.getMethod("subtract", UUID.class, BigDecimal.class);
    }

    @Override
    public String name() {
        return "EssentialsX";
    }

    @Override
    public double balance(OfflinePlayer p) {
        try {
            return ((BigDecimal) getMoney.invoke(null, p.getUniqueId())).doubleValue();
        } catch (Throwable t) {
            return 0;
        }
    }

    @Override
    public boolean has(OfflinePlayer p, double amount) {
        try {
            return (boolean) hasEnough.invoke(null, p.getUniqueId(), BigDecimal.valueOf(amount));
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean withdraw(OfflinePlayer p, double amount) {
        try {
            subtract.invoke(null, p.getUniqueId(), BigDecimal.valueOf(amount));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean deposit(OfflinePlayer p, double amount) {
        try {
            add.invoke(null, p.getUniqueId(), BigDecimal.valueOf(amount));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
