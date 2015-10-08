package me.jaime29010.itemcoder.core;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;

import javax.lang.model.element.Modifier;

public class ItemPaster {
    private static String developerKey;

    public static String pasteCode(Builder builder, String name) throws Exception {
        TypeSpec method = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addMethod(builder.build()).build();
        JavaFile file = JavaFile.builder("", method).build();

        PastebinPaste paste = new PastebinPaste();

        paste.setDeveloperKey(developerKey);
        paste.setContents(file.toString());

        paste.setPasteTitle(name);
        paste.setPasteExpireDate(PasteExpireDate.ONE_HOUR);
        paste.setPasteFormat("java");

        PastebinLink pastebinLink = paste.paste();
        return pastebinLink.getLink().toString();
    }

    public static void setDeveloperKey(String key) {
        developerKey = key;
    }
}
