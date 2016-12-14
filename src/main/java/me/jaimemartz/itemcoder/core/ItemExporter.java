package me.jaimemartz.itemcoder.core;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import me.jaimemartz.itemcoder.Main;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class ItemExporter {
    public static JavaFile exportCode(Builder builder, String name, Main main) throws IOException {
        TypeSpec method = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addMethod(builder.build()).build();
        JavaFile javaFile = JavaFile.builder("", method).build();
        javaFile.writeTo(main.getSnippetsFolder());
        return javaFile;
    }
}
