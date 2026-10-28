package fr.nexshop.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convertit un texte de configuration en Component.
 * Supporte : &c, &l..., &#RRGGBB, &x&R&R..., et MiniMessage (&lt;#ff0000&gt;, &lt;gradient:...&gt;, ...).
 */
public final class TextUtil {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Pattern HEX = Pattern.compile("[&§]#([0-9a-fA-F]{6})");
    private static final Pattern BUNGEE_HEX = Pattern.compile("[&§][xX](?:[&§][0-9a-fA-F]){6}");
    private static final Pattern CODE = Pattern.compile("[&§]([0-9a-fk-orA-FK-OR])");
    private static final Pattern GLYPH = Pattern.compile("<glyph:([A-Za-z0-9_.\\-]+)>");

    private static final Map<Character, String> CODES = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"), Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"), Map.entry('7', "gray"), Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"), Map.entry('a', "green"), Map.entry('b', "aqua"),
            Map.entry('c', "red"), Map.entry('d', "light_purple"), Map.entry('e', "yellow"),
            Map.entry('f', "white"), Map.entry('k', "obfuscated"), Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"), Map.entry('n', "underlined"), Map.entry('o', "italic"),
            Map.entry('r', "reset"));

    private static Function<String, String> glyphResolver = null;

    private TextUtil() {}

    public static void setGlyphResolver(Function<String, String> resolver) {
        glyphResolver = resolver;
    }

    public static String legacyToMini(String s) {
        Matcher m = BUNGEE_HEX.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String hex = m.group().replaceAll("[&§xX]", "");
            m.appendReplacement(sb, Matcher.quoteReplacement("<#" + hex + ">"));
        }
        m.appendTail(sb);
        s = sb.toString();

        m = HEX.matcher(s);
        sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement("<#" + m.group(1) + ">"));
        }
        m.appendTail(sb);
        s = sb.toString();

        m = CODE.matcher(s);
        sb = new StringBuilder();
        while (m.find()) {
            String tag = CODES.get(Character.toLowerCase(m.group(1).charAt(0)));
            m.appendReplacement(sb, Matcher.quoteReplacement("<" + tag + ">"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String resolveGlyphs(String s) {
        if (glyphResolver == null || !s.contains("<glyph:")) return s;
        Matcher m = GLYPH.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String r = null;
            try {
                r = glyphResolver.apply(m.group(1));
            } catch (Throwable ignored) {
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(r != null ? r : m.group()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Parse un texte, italique désactivé par défaut (pour les items). */
    public static Component parse(String s) {
        if (s == null) return Component.empty();
        s = resolveGlyphs(s);
        s = legacyToMini(s);
        Component c = MM.deserialize(s);
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(c);
    }
}
