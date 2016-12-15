package me.jaimemartz.itemcoder.core;

import me.jaimemartz.itemcoder.Main;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import java.io.File;
import java.io.PrintWriter;

public final class ItemDecoder {
    public static ItemStack decode(String name, File input, Main main) {
        File output = new File(main.getSnippetsFolder(), "compile");
        BatchCompiler.compile(new String[] {
                //The file to compile
                input.getAbsolutePath(),

                //Running on 1.7
                "-1.7",

                //The output folder
                "-d", output.getAbsolutePath()
        }, new PrintWriter(System.out), new PrintWriter(System.err), null);
        try {
            Class<?> clazz = main.getSnippetsLoader().loadClass(name);
            File file = new File(output, name + ".class");
            if(file.exists()) {
                file.delete();
            }
            return (ItemStack) clazz.getMethod("getItemStack").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}