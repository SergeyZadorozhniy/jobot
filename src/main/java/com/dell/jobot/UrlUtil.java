package com.dell.jobot;

import lombok.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;

public interface UrlUtil {

	static Optional<URL> convertToUrlWithoutAnchorAndQuery(final @NonNull String raw) {
		String t = raw;
		if(t.contains("#")) {
			t = t.substring(0, t.indexOf("#"));
		}
		if(t.contains("?")) {
			t = t.substring(0, t.indexOf("?"));
		}
		try {
			return Optional.of(new URL(t));
		} catch(final MalformedURLException e) {
			System.err.println("Failed to convert \"" + raw + "\" to URL");
			return Optional.empty();
		} catch(final Exception e) {
			throw new AssertionError("Unexpected failure while converting \"" + raw + "\" to URL", e);
		}
	}

	Predicate<URL> HTTP_FILTER = url -> url.getProtocol().startsWith("http");
}
