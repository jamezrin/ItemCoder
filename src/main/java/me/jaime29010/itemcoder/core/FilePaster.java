package me.jaime29010.itemcoder.core;

import me.jaime29010.itemcoder.Main;
import me.nrubin29.pastebinapi.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FilePaster {
    private static PastebinAPI api = null;
    public static void setAPIKey(String apiKey) {
        api = new PastebinAPI(apiKey);
    }

    public static String paste(String name, File file) throws IOException, PastebinException {
        CreatePaste paste = api.createPaste()
                .withName(name)
                .withFormat(Format.Java)
                .withPrivacyLevel(PrivacyLevel.PUBLIC)
                .withExpireDate(ExpireDate.ONE_HOUR)
                .withFile(file);
        return paste.post();
    }

    public static String paste(String name, String text) throws IOException, PastebinException {
        CreatePaste paste = api.createPaste()
                .withName(name)
                .withFormat(Format.Java)
                .withPrivacyLevel(PrivacyLevel.PUBLIC)
                .withExpireDate(ExpireDate.ONE_HOUR)
                .withText(text);
        return paste.post();
    }
}
