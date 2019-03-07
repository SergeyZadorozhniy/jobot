package com.dell.jobot;

import lombok.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public interface UrlUtil {

	static Optional<URL> convertToUrl(final @NonNull String raw) {
		/* Moved to HyperlinkUtil.java */
		//String t = raw;
		//if(t.contains("#")) {
		//	t = t.substring(0, t.indexOf("#"));
		//}
		//if(t.contains("?")) {
		//	t = t.substring(0, t.indexOf("?"));
		//}
		try {
			return Optional.of(new URL(raw));
		} catch(final MalformedURLException e) {
			System.err.println("Failed to convert \"" + raw + "\" to URL");
			return Optional.empty();
		} catch(final Exception e) {
			throw new AssertionError("Unexpected failure while converting \"" + raw + "\" to URL", e);
		}
	}

	/* Removed as duplicate check */
	//Predicate<URL> HTTP_FILTER = url -> url.getProtocol().equals("http");
}
