package fr.nexshop.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Gestion simple des placeholders %cle%. */
public final class Ph {
    private Ph() {}

    public static Map<String, String> of(Object... kv) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), String.valueOf(kv[i + 1]));
        }
        return map;
    }

    public static String apply(String s, Map<String, String> ph) {
        if (s == null) return null;
        for (Map.Entry<String, String> e : ph.entrySet()) {
            s = s.replace("%" + e.getKey() + "%", e.getValue());
        }
        return s;
    }

    public static String pretty(String spec) {
        if (spec == null) return "";
        String s = spec;
        int idx = s.indexOf(':');
        if (idx >= 0) s = s.substring(idx + 1);
        String[] words = s.replace('_', ' ').replace('-', ' ').toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
