package fr.nexshop.hook;

import fr.nexshop.ShopPlugin;
import fr.nexshop.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/** Intégration Nexo par réflexion (aucune dépendance à la compilation). */
public final class NexoHook {
    private boolean enabled;
    private Object instance;
    private Method itemFromId;
    private Method idFromItem;
    private java.util.logging.Logger log;

    public NexoHook(ShopPlugin plugin) {
        this.log = plugin.getLogger();
        if (Bukkit.getPluginManager().getPlugin("Nexo") == null) return;
        try {
            Class<?> items = Class.forName("com.nexomc.nexo.api.NexoItems");
            try {
                instance = items.getField("INSTANCE").get(null);
            } catch (Exception ignored) {
                instance = null;
            }
            itemFromId = items.getMethod("itemFromId", String.class);
            idFromItem = items.getMethod("idFromItem", ItemStack.class);
            enabled = true;
            TextUtil.setGlyphResolver(this::glyph);
            TextUtil.setNexoMiniMessage(findNexoMiniMessage());
            plugin.getLogger().info("Nexo détecté : items nexo:<id> disponibles.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Nexo détecté mais API introuvable : " + t.getMessage());
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public ItemStack build(String id) {
        if (!enabled) return null;
        try {
            Object builder = itemFromId.invoke(instance, id);
            if (builder == null) return null;
            Object r = builder.getClass().getMethod("build").invoke(builder);
            return r instanceof ItemStack s ? s.clone() : null;
        } catch (Throwable t) {
            return null;
        }
    }

    public String idOf(ItemStack stack) {
        if (!enabled || stack == null) return null;
        try {
            Object r = idFromItem.invoke(instance, stack);
            return r == null ? null : r.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    /** Cherche le MiniMessage de Nexo (gère <shift:N>, <glyph:..>) ; renvoie null si introuvable. */
    private net.kyori.adventure.text.minimessage.MiniMessage findNexoMiniMessage() {
        try {
            Class<?> au = Class.forName("com.nexomc.nexo.utils.AdventureUtils");
            Object inst = null;
            try {
                inst = au.getField("INSTANCE").get(null);
            } catch (Throwable ignored) {
            }
            java.util.List<Object> found = new java.util.ArrayList<>();
            for (java.lang.reflect.Field f : au.getDeclaredFields()) {
                if (net.kyori.adventure.text.minimessage.MiniMessage.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        found.add(f.get(java.lang.reflect.Modifier.isStatic(f.getModifiers()) ? null : inst));
                    } catch (Throwable ignored) {
                    }
                }
            }
            for (Method m : au.getMethods()) {
                if (m.getParameterCount() == 0 && net.kyori.adventure.text.minimessage.MiniMessage.class.isAssignableFrom(m.getReturnType())) {
                    try {
                        found.add(m.invoke(java.lang.reflect.Modifier.isStatic(m.getModifiers()) ? null : inst));
                    } catch (Throwable ignored) {
                    }
                }
            }
            for (Object o : found) {
                if (!(o instanceof net.kyori.adventure.text.minimessage.MiniMessage mm)) continue;
                String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(mm.deserialize("<shift:-1>"));
                if (!plain.contains("shift")) return mm;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /** Résolution best-effort de <glyph:nom> en "<white><font:..>caractères</font>". */
    private String glyph(String name) {
        try {
            Class<?> nexo = Class.forName("com.nexomc.nexo.NexoPlugin");
            Object pl = call(nexo, null, "instance");
            Object fm = call(pl.getClass(), pl, "fontManager");
            Object g = call(fm.getClass(), fm, "glyphFromName", name);
            if (g == null) {
                log.warning("Glyph Nexo introuvable : '" + name + "'");
                return null;
            }
            String chars = null;
            for (String m : new String[]{"getCharacter", "character", "getChars", "chars"}) {
                try {
                    Object c = call(g.getClass(), g, m);
                    if (c != null) {
                        chars = c instanceof char[] arr ? new String(arr) : String.valueOf(c);
                        break;
                    }
                } catch (Throwable ignored) {
                }
            }
            if (chars == null || chars.isEmpty()) {
                log.warning("Glyph Nexo '" + name + "' : caractères introuvables.");
                return null;
            }
            String font = null;
            for (String m : new String[]{"getFont", "font"}) {
                try {
                    Object f = call(g.getClass(), g, m);
                    if (f != null) {
                        font = f.toString();
                        break;
                    }
                } catch (Throwable ignored) {
                }
            }
            if (font != null && font.contains("[")) font = null; // toString inutilisable
            StringBuilder cp = new StringBuilder();
            chars.codePoints().forEach(c -> cp.append(String.format("U+%04X ", c)));
            log.info("Glyph '" + name + "' -> " + cp.toString().trim() + " (font: " + font + ")");
            String out = chars;
            if (font != null && !font.isBlank() && !font.equals("default") && !font.equals("minecraft:default")) {
                if (!font.contains(":")) font = "minecraft:" + font;
                out = "<font:" + font + ">" + chars + "</font>";
            }
            return "<white>" + out + "</white>";
        } catch (Throwable t) {
            log.warning("Résolution du glyph '" + name + "' impossible : " + t);
        }
        return null;
    }

    private static Object call(Class<?> c, Object target, String name, Object... args) throws Exception {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                return m.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(name);
    }
}
