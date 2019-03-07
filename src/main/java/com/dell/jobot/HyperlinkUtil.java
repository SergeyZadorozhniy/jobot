package com.dell.jobot;

import lombok.NonNull;
import lombok.val;
import java.net.URL;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface HyperlinkUtil {

	String GROUP_NAME_VALUE = "value";
	String PROTOCOL = "http://";
	int PROTOCOL_LENGTH = PROTOCOL.length();
	Pattern PATTERN_HREF_VALUE = Pattern.compile("href=\"(?<" + GROUP_NAME_VALUE + ">"+PROTOCOL+"[^\"]{8,256})\"");

	static void extractLinks(final @NonNull URL parentUrl, final @NonNull String text, final @NonNull Collection<String> internalUrlsBuff, final @NonNull Collection<String> exterbalUrlsBuff) {
		val matcher = PATTERN_HREF_VALUE.matcher(text);
		while (matcher.find()) {
			String url = matcher.group(GROUP_NAME_VALUE);

			url = cutAnchorAndQuery(url);

			switch (url.substring(url.length() - 4).toLowerCase()) {
				case ".jpg":
				case ".png":
				case ".flv":
				case ".pdf":
				case ".zip":
				case ".gif":
				case ".ico":
					continue;
			}

			if (url.indexOf(parentUrl.getHost().toString()) == PROTOCOL_LENGTH)
				internalUrlsBuff.add(url);
			else
				exterbalUrlsBuff.add(url);
		}
	}

	static String cutAnchorAndQuery(@NonNull String urlString) {
		/* Moved from UrlUtils.java here - more right place */
		if (urlString.contains("#"))
			urlString = urlString.substring(0, urlString.indexOf("#"));
		if (urlString.contains("?"))
			urlString = urlString.substring(0, urlString.indexOf("?"));

		return urlString;
	}

	Predicate<String> HTTP_FILTER = url -> url.indexOf("http://") == 0;
}
