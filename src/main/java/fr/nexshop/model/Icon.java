package fr.nexshop.model;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/** Description générique d'un item de menu, lue depuis la config. */
public final class Icon {
    public final String material;   // "STONE" ou "nexo:id"
    public final String name;       // nullable
    public final List<String> lore; // nullable
    public final int amount;
    public final Integer customModelData;
    public final boolean glow;
    public final boolean hideFlags;
    public final List<Integer> slots;
    public final String action;     // CLOSE / MAIN / BACK / null
    public final List<String> commands;

    private Icon(String material, String name, List<String> lore, int amount, Integer cmd, boolean glow,
                 boolean hideFlags, List<Integer> slots, String action, List<String> commands) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.customModelData = cmd;
        this.glow = glow;
        this.hideFlags = hideFlags;
        this.slots = slots;
        this.action = action;
        this.commands = commands;
    }

    public static Icon load(ConfigurationSection s, String defMaterial) {
        String material = s.getString("display-material", s.getString("material", defMaterial));
        if (material == null) material = "STONE";
        String name = s.getString("name");
        List<String> lore = null;
        if (s.isList("lore")) lore = s.getStringList("lore");
        else if (s.isString("lore")) lore = List.of(s.getString("lore"));
        int amount = s.get("amount") instanceof Number n ? n.intValue() : 1;
        Integer cmd = s.get("custom-model-data") instanceof Number n ? n.intValue() : null;
        boolean glow = s.getBoolean("glow", false);
        boolean hide = s.getBoolean("hide-flags", true);
        return new Icon(material, name, lore, Math.max(1, amount), cmd, glow, hide, parseSlots(s),
                s.getString("action"), s.getStringList("commands"));
    }

    private static List<Integer> parseSlots(ConfigurationSection s) {
        List<Integer> out = new ArrayList<>();
        if (s.contains("slot")) addSlot(out, s.get("slot"));
        if (s.contains("slots")) {
            Object o = s.get("slots");
            if (o instanceof List<?> l) l.forEach(x -> addSlot(out, x));
            else addSlot(out, o);
        }
        return out;
    }

    private static void addSlot(List<Integer> out, Object o) {
        if (o instanceof Number n) {
            out.add(n.intValue());
            return;
        }
        String str = String.valueOf(o).trim();
        try {
            if (str.contains("-") && !str.startsWith("-")) {
                String[] p = str.split("-");
                int a = Integer.parseInt(p[0].trim());
                int b = Integer.parseInt(p[1].trim());
                for (int i = Math.min(a, b); i <= Math.max(a, b); i++) out.add(i);
            } else {
                out.add(Integer.parseInt(str));
            }
        } catch (NumberFormatException ignored) {
        }
    }
}
