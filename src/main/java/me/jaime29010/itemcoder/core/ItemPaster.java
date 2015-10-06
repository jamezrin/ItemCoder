package me.jaime29010.itemcoder.core;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import me.jaime29010.itemcoder.Main;
import me.nrubin29.pastebinapi.PastebinException;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class ItemPaster {
    public static String pasteCode(Builder builder, String name) throws IOException, PastebinException {
        TypeSpec method = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addMethod(builder.build()).build();
        JavaFile javaFile = JavaFile.builder("", method).build();
        return FilePaster.paste(name, javaFile.toString());
    }
}
