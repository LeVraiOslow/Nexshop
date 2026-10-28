package fr.nexshop.util;

import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Money {
    private final DecimalFormat df;
    private final String display;

    public Money(FileConfiguration cfg) {
        this.df = new DecimalFormat(cfg.getString("currency.pattern", "#,##0.##"),
                DecimalFormatSymbols.getInstance(Locale.US));
        this.display = cfg.getString("currency.display", "%amount%$");
    }

    public String format(double value) {
        return display.replace("%amount%", df.format(value));
    }

    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
