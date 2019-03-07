package com.dell.jobot;

import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.regex.Pattern;

public interface HyperlinkUtil {

	String GROUP_NAME_VALUE = "value";

	Pattern PATTERN_HREF_VALUE = Pattern.compile("href=\"(?<" + GROUP_NAME_VALUE + ">http://[\\S^\"]{8,256})\"");

	static void extractLinks(final @NonNull String text, final @NonNull Collection<String> dstBuff) {
		val matcher = PATTERN_HREF_VALUE.matcher(text);
		while(matcher.find()) {
			val url = matcher.group(GROUP_NAME_VALUE);
			dstBuff.add(url);
		}
	}
}
