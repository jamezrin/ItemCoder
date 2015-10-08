package me.jaime29010.itemcoder;

import me.jaime29010.itemcoder.Messager.Replacer;

public class Prefixer {
    private static String prefix;

    public static void setPrefix(String prefix) {
        Prefixer.prefix = prefix;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static Replacer getReplacer() {
        return Replacer.create("{prefix}", prefix);
    }
}
