package me.jaime29010.itemcoder.core;

import com.squareup.javapoet.JavaFile;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class ItemDecoder {
    /*
    TODO: Compiling the .java file to .class
    http://www.dreamincode.net/forums/topic/119046-runtime-compile-and-loading-of-java-class-files/

    TODO: Loading the .class
    http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html

    TODO: Running the static method
    http://www.javaworld.com/article/2077455/learn-java/dynamically-invoking-a-static-method-without-instance-reference-july-6-1999.html
    */

    public static ItemStack decode(JavaFile file) {
        return null;
    }

    private static Class<?> compile(File file) {
        return null;
    }

    private static void loadClass(Class<?> clazz) {

    }
}
