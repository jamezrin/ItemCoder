package me.nrubin29.pastebinapi.example;

import me.nrubin29.pastebinapi.*;

import java.io.IOException;
import java.util.ArrayList;

public class Example {

	public static void main(String[] args) throws PastebinException, IOException {
		PastebinAPI api = new PastebinAPI("e3ff18d8fb001a3ece08ae0d7d4a87bd");

		CreatePaste paste = api.createPaste()
				.withName("Paste name")
				.withFormat(Format.None)
				.withPrivacyLevel(PrivacyLevel.PUBLIC)
				.withExpireDate(ExpireDate.NEVER)
				.withFile(null);
		String url = paste.post();
	}
}

