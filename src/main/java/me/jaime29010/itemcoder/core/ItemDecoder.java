package me.jaime29010.itemcoder.core;

import com.squareup.javapoet.JavaFile;
import me.jaime29010.itemcoder.Main;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.core.JDTCompilerAdapter;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import javax.tools.JavaCompiler;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public final class ItemDecoder {
    /*
    TODO: Compiling the .java file to .class
    http://www.dreamincode.net/forums/topic/119046-runtime-compile-and-loading-of-java-class-files/

    TODO: Loading the .class
    http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html

    TODO: Running the static method
    http://www.javaworld.com/article/2077455/learn-java/dynamically-invoking-a-static-method-without-instance-reference-july-6-1999.html
    */

    public static ItemStack decode(String name, File input, Main main) {
        File output = new File(main.getSnippetsFolder(), "compile");
        //Compile...
        BatchCompiler.compile(new String[] {
                //The file to compile
                input.getAbsolutePath(),

                //Running on 1.7
                "-1.7",

                //The output folder
                "-d", output.getAbsolutePath()
        }, new PrintWriter(System.out), new PrintWriter(System.err), null);
        try {
            Class clazz = main.getSnippetsLoader().loadClass(name);
            File file = new File(output, name + ".class");
            if(file.exists()) {
                file.delete();
            }
            ItemStack item = (ItemStack) clazz.getMethod("getItemStack").invoke(null, null);
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}